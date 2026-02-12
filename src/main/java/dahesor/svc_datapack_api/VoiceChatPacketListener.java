package dahesor.svc_datapack_api;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

    public class VoiceChatPacketListener implements VoicechatPlugin {
        // 存储每个玩家上次添加tag的时间戳
        private static final ConcurrentHashMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();
        // 冷却时间（毫秒）
        private static final long COOLDOWN_MS = 40L;

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
            if (senderConn == null) return;

            var vcPlayer = senderConn.getPlayer();

            // 创建 final 或 effectively final 的变量用于 lambda
            final net.minecraft.server.level.ServerPlayer finalPlayer = (net.minecraft.server.level.ServerPlayer) vcPlayer.getPlayer();
            final UUID playerUUID = finalPlayer.getUUID();

            // 快速冷却检查
            long currentTime = System.currentTimeMillis();
            Long lastTime = cooldowns.get(playerUUID);
            if (lastTime != null && (currentTime - lastTime) < COOLDOWN_MS) {
                return;
            }

            MinecraftServer server = finalPlayer.level().getServer();

            // 关键：回到主线程，使用 final 变量
            server.execute(() -> {
                // 主线程中最终检查并执行
                long now = System.currentTimeMillis();
                Long last = cooldowns.get(playerUUID);

                if (last != null && (now - last) < COOLDOWN_MS) {
                    return; // 仍在冷却期
                }

                if (!finalPlayer.getTags().contains("svc_api.talked")) {
                    finalPlayer.addTag("svc_api.talked");
                    cooldowns.put(playerUUID, now);
                }
            });
        }

        // 可选：定期清理旧的冷却记录，防止内存泄漏
        public static void cleanupOldCooldowns() {
            long currentTime = System.currentTimeMillis();
            long cleanupThreshold = 60000L; // 清理1分钟前的记录

            cooldowns.entrySet().removeIf(entry ->
                    (currentTime - entry.getValue()) > cleanupThreshold
            );
        }
    }
