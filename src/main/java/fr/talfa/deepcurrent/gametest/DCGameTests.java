package fr.talfa.deepcurrent.gametest;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.talfa.deepcurrent.DeepCurrent;
import fr.talfa.deepcurrent.config.DCConfigs;
import fr.talfa.deepcurrent.core.pressure.PressureTiers;
import fr.talfa.deepcurrent.world.Tides;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec3;

//? if neoforge {
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
//?} else {
/*import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
*///?}

/**
 * In-world checks run by {@code runGameTestServer}: the mod loads, its config is read and its commands work.
 * Only registered when the {@code deepcurrent} game test namespace is enabled (dev runs and CI).
 */
@GameTestHolder(DeepCurrent.ID)
@PrefixGameTestTemplate(false)
public final class DCGameTests {
    private DCGameTests() {
    }

    @GameTest(template = "empty")
    public static void serverConfigIsLoaded(GameTestHelper helper) {
        PressureTiers tiers = DCConfigs.server().pressureTiers();
        helper.assertTrue(tiers.maxDepth(1) == 16, "tier 1 depth should default to 16, got " + tiers.maxDepth(1));
        helper.assertTrue(DCConfigs.server().stressPerKilowatt.get() == 256, "stressPerKilowatt should be 256");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tideCommandForcesAndReleasesTheTide(GameTestHelper helper) {
        try {
            run(helper, "deepcurrent tide");
            run(helper, "deepcurrent tide 0.5");
            helper.assertTrue(Tides.isForced(), "tide should be forced");
            helper.assertTrue(Tides.coefficient(helper.getLevel()) == 0.5, "forced coefficient should be 0.5");
            run(helper, "deepcurrent tide reset");
            helper.assertFalse(Tides.isForced(), "tide should follow the moon again");
            double natural = Tides.coefficient(helper.getLevel());
            helper.assertTrue(natural >= 0 && natural <= 1, "natural coefficient out of range: " + natural);
        } finally {
            Tides.release();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pressureCommandRuns(GameTestHelper helper) {
        run(helper, "deepcurrent pressure");
        helper.succeed();
    }

    private static void run(GameTestHelper helper, String command) {
        MinecraftServer server = helper.getLevel().getServer();
        CommandSourceStack source = server.createCommandSourceStack()
                .withLevel(helper.getLevel())
                .withPosition(helper.absoluteVec(Vec3.ZERO))
                .withSuppressedOutput();
        try {
            int result = server.getCommands().getDispatcher().execute(command, source);
            helper.assertTrue(result > 0, "/" + command + " returned " + result);
        } catch (CommandSyntaxException e) {
            helper.fail("/" + command + " failed: " + e.getMessage());
        }
    }
}
