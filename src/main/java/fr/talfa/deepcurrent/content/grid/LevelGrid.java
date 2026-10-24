package fr.talfa.deepcurrent.content.grid;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.talfa.deepcurrent.DeepCurrent;
import fr.talfa.deepcurrent.config.DCConfigs;
import fr.talfa.deepcurrent.core.grid.GridGraph;
import fr.talfa.deepcurrent.core.grid.GridNetwork;
import fr.talfa.deepcurrent.core.grid.GridSettings;
import fr.talfa.deepcurrent.core.grid.GridSolution;
import fr.talfa.deepcurrent.core.grid.NodeRole;
import fr.talfa.deepcurrent.core.grid.VoltageClass;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

//? if >=1.21 {
import net.minecraft.core.HolderLookup;
//?}

/**
 * The electrical grid of one dimension: the nodes and cables saved with the world, and the live {@link GridGraph}
 * built from them. Block entities register their node when they load and push their offer or demand; the graph
 * itself only changes when nodes or cables are added or removed.
 */
public final class LevelGrid extends SavedData {
    private static final String NAME = DeepCurrent.ID + "_grid";

    private final Map<Long, NodeRole> nodes = new LinkedHashMap<>();
    /** Cables keyed by their ordered endpoints. */
    private final Map<Key, StoredCable> cables = new LinkedHashMap<>();
    private GridGraph graph;

    private LevelGrid() {
    }

    public static LevelGrid get(ServerLevel level) {
        //? if >=1.21 {
        LevelGrid grid = level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(LevelGrid::new, LevelGrid::load, null), NAME);
        //?} else
        /*LevelGrid grid = level.getDataStorage().computeIfAbsent(LevelGrid::load, LevelGrid::new, NAME);*/
        grid.refreshSettings();
        return grid;
    }

    /** A cable as saved: a bond is the zero-length link between a connector and the machine it sits on. */
    public record StoredCable(long a, long b, double length, VoltageClass voltage, int cost, boolean bond) {
        public long other(long node) {
            return node == a ? b : a;
        }
    }

    private record Key(long low, long high) {
        static Key of(long a, long b) {
            return a < b ? new Key(a, b) : new Key(b, a);
        }
    }

    // --- Queries ------------------------------------------------------------------------------

    public GridGraph graph() {
        return graph;
    }

    public boolean hasNode(BlockPos pos) {
        return nodes.containsKey(pos.asLong());
    }

    public NodeRole role(BlockPos pos) {
        return nodes.get(pos.asLong());
    }

    public GridNetwork network(BlockPos pos) {
        return graph.networkOf(pos.asLong());
    }

    public GridSolution solution(BlockPos pos) {
        return graph.solutionOf(pos.asLong());
    }

    public boolean linked(BlockPos a, BlockPos b) {
        return cables.containsKey(Key.of(a.asLong(), b.asLong()));
    }

    /** Cables attached to a node, bonds included. */
    public List<StoredCable> cablesOf(BlockPos pos) {
        long id = pos.asLong();
        List<StoredCable> result = new ArrayList<>();
        for (StoredCable cable : cables.values()) {
            if (cable.a() == id || cable.b() == id) {
                result.add(cable);
            }
        }
        return result;
    }

    // --- Changes ------------------------------------------------------------------------------

    /** Registers a node if it is not known yet; a node whose role changed is replaced and keeps its cables. */
    public void ensureNode(BlockPos pos, NodeRole role) {
        long id = pos.asLong();
        NodeRole known = nodes.get(id);
        if (known == role) {
            return;
        }
        List<StoredCable> attached = known == null ? List.of() : cablesOf(pos);
        if (known != null) {
            graph.removeNode(id);
        }
        nodes.put(id, role);
        graph.addNode(id, role);
        for (StoredCable cable : attached) {
            if (graph.hasNode(cable.other(id))) {
                graph.connect(cable.a(), cable.b(), cable.length(), cable.voltage());
            }
        }
        setDirty();
    }

    /** Removes a node and returns the cables it had, bonds included. */
    public List<StoredCable> removeNode(BlockPos pos) {
        long id = pos.asLong();
        if (nodes.remove(id) == null) {
            return List.of();
        }
        List<StoredCable> removed = cablesOf(pos);
        for (StoredCable cable : removed) {
            cables.remove(Key.of(cable.a(), cable.b()));
        }
        graph.removeNode(id);
        setDirty();
        return removed;
    }

    /** Lays a cable between two registered nodes; returns false if they are already joined. */
    public boolean link(BlockPos a, BlockPos b, double length, VoltageClass voltage, int cost) {
        return add(new StoredCable(a.asLong(), b.asLong(), length, voltage, cost, false));
    }

    /** Joins a connector to the machine it is attached to. */
    public boolean bond(BlockPos connector, BlockPos machine, VoltageClass voltage) {
        return add(new StoredCable(connector.asLong(), machine.asLong(), 0, voltage, 0, true));
    }

    /** Removes the cables of a node, keeping its bonds; returns what was removed. */
    public List<StoredCable> unlinkAll(BlockPos pos) {
        List<StoredCable> removed = new ArrayList<>();
        for (StoredCable cable : cablesOf(pos)) {
            if (!cable.bond()) {
                cables.remove(Key.of(cable.a(), cable.b()));
                graph.disconnect(cable.a(), cable.b());
                removed.add(cable);
            }
        }
        if (!removed.isEmpty()) {
            setDirty();
        }
        return removed;
    }

    private boolean add(StoredCable cable) {
        Key key = Key.of(cable.a(), cable.b());
        if (cables.containsKey(key) || !nodes.containsKey(cable.a()) || !nodes.containsKey(cable.b())) {
            return false;
        }
        cables.put(key, cable);
        graph.connect(cable.a(), cable.b(), cable.length(), cable.voltage());
        setDirty();
        return true;
    }

    // --- Persistence --------------------------------------------------------------------------

    private void refreshSettings() {
        GridSettings settings = DCConfigs.server() != null ? DCConfigs.server().gridSettings() : GridSettings.DEFAULT;
        if (graph == null) {
            graph = new GridGraph(settings);
            for (Map.Entry<Long, NodeRole> node : nodes.entrySet()) {
                graph.addNode(node.getKey(), node.getValue());
            }
            for (StoredCable cable : cables.values()) {
                graph.connect(cable.a(), cable.b(), cable.length(), cable.voltage());
            }
        } else if (!graph.settings().equals(settings)) {
            graph.setSettings(settings);
        }
    }

    //? if >=1.21 {
    private static LevelGrid load(CompoundTag tag, HolderLookup.Provider registries) {
    //?} else
    /*private static LevelGrid load(CompoundTag tag) {*/
        LevelGrid grid = new LevelGrid();
        for (Tag entry : tag.getList("Nodes", Tag.TAG_COMPOUND)) {
            CompoundTag node = (CompoundTag) entry;
            grid.nodes.put(node.getLong("Pos"), NodeRole.valueOf(node.getString("Role")));
        }
        for (Tag entry : tag.getList("Cables", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) entry;
            StoredCable cable = new StoredCable(c.getLong("A"), c.getLong("B"), c.getDouble("Length"),
                    VoltageClass.valueOf(c.getString("Voltage")), c.getInt("Cost"), c.getBoolean("Bond"));
            if (grid.nodes.containsKey(cable.a()) && grid.nodes.containsKey(cable.b())) {
                grid.cables.put(Key.of(cable.a(), cable.b()), cable);
            }
        }
        return grid;
    }

    @Override
    //? if >=1.21 {
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
    //?} else
    /*public CompoundTag save(CompoundTag tag) {*/
        ListTag nodeList = new ListTag();
        for (Map.Entry<Long, NodeRole> node : nodes.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Pos", node.getKey());
            entry.putString("Role", node.getValue().name());
            nodeList.add(entry);
        }
        tag.put("Nodes", nodeList);
        ListTag cableList = new ListTag();
        for (StoredCable cable : cables.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("A", cable.a());
            entry.putLong("B", cable.b());
            entry.putDouble("Length", cable.length());
            entry.putString("Voltage", cable.voltage().name());
            entry.putInt("Cost", cable.cost());
            entry.putBoolean("Bond", cable.bond());
            cableList.add(entry);
        }
        tag.put("Cables", cableList);
        return tag;
    }

    /** Round-trips the saved data, for game tests. */
    public LevelGrid reloadedCopy() {
        //? if >=1.21 {
        LevelGrid copy = load(save(new CompoundTag(), null), null);
        //?} else
        /*LevelGrid copy = load(save(new CompoundTag()));*/
        copy.refreshSettings();
        return copy;
    }
}
