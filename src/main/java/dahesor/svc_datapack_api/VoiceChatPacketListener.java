package dahesor.svc_datapack_api;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;

import java.util.UUID;

public class VoiceChatPacketListener implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return "svc_datapack_api";
    }

    @Override
    public void initialize(VoicechatApi api) {
        // 这里能拿到 VoicechatApi，需要时可存起来
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicPacket);
    }

    private void onMicPacket(MicrophonePacketEvent event) {
            VoicechatConnection senderConn = event.getSenderConnection();
        if (senderConn == null) return; // 不是玩家发来的就忽略

        de.maxhenkel.voicechat.api.ServerPlayer vcPlayer = senderConn.getPlayer();

        // 取原生 ServerPlayer
        net.minecraft.server.level.ServerPlayer player =
                (net.minecraft.server.level.ServerPlayer) vcPlayer.getPlayer();

        player.addTag("svc_api.talked");

    }
}
