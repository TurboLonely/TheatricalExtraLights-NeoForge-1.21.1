package com.github.dumann089.theatricalextralights.forge;

import com.github.dumann089.theatricalextralights.client.followspot.FollowspotCameraAccess;
import com.github.dumann089.theatricalextralights.client.followspot.FollowspotFixtureCameraSession;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge camera hook for the followspot camera session.
 *
 * <p>Kept in the {@code ...theatricalextralights.forge} package on purpose: the common code looks
 * this class up reflectively by that exact name (see
 * {@code FollowspotFixtureCameraSession#registerPlatformCameraHook}).
 */
public final class FollowspotCameraForge {

    private static boolean listenerRegistered;

    private FollowspotCameraForge() {
    }

    public static void ensureRegistered() {
        if (listenerRegistered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, FollowspotCameraForge::onComputeCameraAngles);
        listenerRegistered = true;
    }

    private static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!FollowspotFixtureCameraSession.isActive()) {
            return;
        }
        FollowspotFixtureCameraSession.CameraState state = FollowspotFixtureCameraSession.getActive().getCameraState();
        if (state == null) {
            return;
        }
        FollowspotCameraAccess.tryApplyCameraState(event.getCamera(), state.position(), state.yaw(), state.pitch());
        event.setYaw(state.yaw());
        event.setPitch(state.pitch());
        event.setRoll(0);
    }
}
