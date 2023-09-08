package ru.voidpointer.paper.voidemoji.resourcepack;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.net.InetSocketAddress;

import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;

@SuppressWarnings("HttpUrlsUsage") /* TLS not supported (and likely won't be) */
@ConfigSerializable
public record ResourcePackConfig(
        @Comment("Defines how the pack should be distributed.")
        DistributionSettings distribution,
        @Comment("""
        Resource pack hosting server settings.

        The resulting resource pack download link will be http://{address}:{port}""")
        HttpSettings http,
        @Comment("Network protection settings.")
        FirewallSettings firewall
) {
    @ConfigSerializable
    record DistributionSettings(
            @Comment("""
            Whether to use a built in resource pack server or a custom URL when sending
            resource pack updates to players.

            BUILT_IN        - starts a built-in standard Java HttpServer hosting resource pack
                located at plugins/{ThisPlugin}/{pack-filename} using settings from http section
                of this config.
            STATIC_PACK_URL - utilizes the configured custom-url setting as direct resource pack
                download link and configured sha1 setting as a SHA1 hash sum for the download
                file validation on client side.
            API_URL         - A planned feature, stay tuned. Will be capable of providing
                dynamic hash sum packs (i.e. if your pack should differ from player to player
                or if you'd like to transfer distribution logic to an API server).
            ADDON           - A planned feature, stay tuned. Will introduce a plugin API that
                will delegate distribution logic to your desired needs through another plugins
                registering themselves as addons or via plugin's custom addon loading system.""")
            Mode mode,
            @Comment("The name of your .zip resource pack archive located in the plugin's data folder.")
            String packFilename,
            @Comment("""
            Either a direct download URL to the resource pack or an API endpoint depending on the mode.

            Supports the following placeholders:
            > {player}   - the name of a player that will receive resource pack send packet.
            > {playerip} - an IP address of the player's connection.
            > {language} - a language from player's preferred locale.""")
            String url,
            @Comment("""
            """)
            String sha1
    ) {
        public enum Mode { BUILT_IN, STATIC_PACK_URL }
        public DistributionSettings() {
            this(Mode.BUILT_IN,
                    "pack.zip",
                    "https://void.pointer/?server=global&player={player}&uuid={uuid}&playerip={playerip}" +
                            "&language={language}",
                    "09184bee090ec199a85b7f5a231ddb39fc4ca125"
            );
        }
        public Mode mode() {
            return mode != null ? mode : Mode.BUILT_IN;
        }
    }

    @ConfigSerializable
    record HttpSettings(
            String address,
            int port,
            @Comment("""
            The maximum number of connections for TPC/IP socket.

            This value cannot be higher that your somaxconn system parameter (OS
            will automatically span it to it's maximum). Tweaking and getting parameter
            on Linux is possible through /proc/sys/net/core/somaxconn file or /etc/sysctl.conf
            to make it persistent after reboots.

            Leave it to zero if you don't know what's that about, your OS will make
            the decision for you.""")
            int backlog,
            @Comment("The maximum time in seconds to wait until exchanges have finished.")
            int stopDelayInSeconds,
            @Comment("""
            The number of threads that will handle download requests.
            
            As the load here is I/O-bound, you can safely set it to adequately high numbers.
            By default (if worker-threads < 1) it will utilize a quarter of available threads
            or 4 threads if there's not enough CPU core.
            When running on a desktop CPUs with low core number you may want to increase the
            value from a quarter, while on a typical server CPU with more cores - 32, 64 and,
            possibly, even more, you might want to adjust it to your needs.
            
            In general the number of threads should depend on an estimated number of players
            (resp. the est. max num. of requests/min)""")
            int workerThreads
    ) {
        public HttpSettings() {
            this("0.0.0.0",
                    8080,
                    0,
                    10,
                    (int) ceil(max(getRuntime().availableProcessors() * 0.25, 4))
            );
        }

        public InetSocketAddress inetAddress() {
            return new InetSocketAddress(address, port);
        }

        public int workerThreads() {
            return workerThreads < 1 ? (int) ceil(max(getRuntime().availableProcessors() * 0.25, 4)) : workerThreads;
        }

        public int stopDelayInSeconds() {
            return stopDelayInSeconds < 0 ? 0 : stopDelayInSeconds;
        }
    }

    @ConfigSerializable
    record FirewallSettings(
            @Comment("""
            Controls whether the HTTP server should only allow pack downloads for
            currently connected players.

            This will block all unknown internet addresses from connecting, downloading
            and correspondingly affecting network available bandwidth.

            Disabling this allows anyone on the internet access the server and download the
            resource pack, making your server vulnerable to DDoS attacks. Use at your own
            risk ONLY if you absolutely HAVE TO to host the resource pack and have NO OTHER
            available services/methods of hosing it. You should consider hosting your own nginx
            server, any kind of public access blob storage, a could or third-party service.""")
            boolean onlyPlayersCanDownload,
            @Comment("""
            Further restricts access to resource pack file downloading by only processing
            download when the Plugin sends the pack to a connected player.

            This option only works in conjunction with only-players-can-download and if that
            option is disabled, the only-on-pack-send will not be respected by the plugin.

            Disabling this allows connected players to download the pack as many times and
            at any given point in time as they want to, making it vulnerable to DDoS attacks.""")
            boolean onlyOnPackSend
    ) {
        public FirewallSettings() {
            this(true, true);
        }

        public boolean enabled() {
            return onlyPlayersCanDownload;
        }
    }

    public ResourcePackConfig() {
        this(new DistributionSettings(), new HttpSettings(), new FirewallSettings());
    }
}
