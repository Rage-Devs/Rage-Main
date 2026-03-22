package ki.rage.minecraft;

import ki.rage.client.Client;
import ki.rage.feature.module.impl.misc.ExtraTab;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Nullables;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

import static ki.rage.client.util.IMinecraft.mc;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    private static final Comparator<Object> ENTRY_ORDERING = Comparator.comparingInt((entry) -> ((PlayerListEntry) entry).getGameMode() == GameMode.SPECTATOR ? 1 : 0)
            .thenComparing((entry) -> Nullables.mapOrElse(((PlayerListEntry) entry).getScoreboardTeam(), Team::getName, ""))
            .thenComparing((entry) -> ((PlayerListEntry) entry).getProfile().name(), String::compareToIgnoreCase);

    @Inject(method = "collectPlayerEntries", at = @At("HEAD"), cancellable = true)
    private void collectPlayerEntriesHook(CallbackInfoReturnable<List<PlayerListEntry>> cir) {
        ExtraTab extraTab = Client.getInstance().getModuleManager().get(ExtraTab.class);
        if (extraTab.enabled())
            cir.setReturnValue(mc.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).limit(1000).toList());
        else
            cir.setReturnValue(mc.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).limit(80).toList());
    }
}