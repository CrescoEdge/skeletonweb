package io.cresco.skeletonweb;

import io.cresco.library.agent.AgentService;
import io.cresco.library.messaging.MsgEvent;
import io.cresco.library.plugin.Executor;
import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.plugin.PluginService;
import io.cresco.library.utilities.CLogger;
import io.cresco.skeletonweb.controllers.Example;
import io.cresco.skeletonweb.filters.NotFoundExceptionHandler;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Skeleton web plugin: a Cresco plugin that serves a JAX-RS (Jersey 3) web application from an
 * embedded Jetty 12 server (ee10 servlet environment). Copy this project to start a plugin with a
 * web UI or REST API: add resources next to {@link Example} and register them in {@link #startWebServer()}.
 *
 * <p>Plugin configuration (CADL node params or {@code pluginadd} configparams):
 * <ul>
 *   <li>{@code web_port} - HTTP port (default 8182)</li>
 *   <li>{@code web_host} - bind address (default all interfaces)</li>
 * </ul>
 */
@Component(
        service = { PluginService.class },
        scope = ServiceScope.PROTOTYPE,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        reference = @Reference(name = "io.cresco.library.agent.AgentService", service = AgentService.class)
)
public class Plugin implements PluginService {

    /** Shared with the JAX-RS resources, which Jersey instantiates per request. */
    public static volatile PluginBuilder pluginBuilder;

    public static final int DEFAULT_WEB_PORT = 8182;

    private BundleContext context;
    private Map<String, Object> map;
    private Executor executor;
    private CLogger logger;
    private Server jettyServer;
    private final AtomicBoolean isActive = new AtomicBoolean(false);

    @Activate
    void activate(BundleContext context, Map<String, Object> map) {
        this.context = context;
        this.map = map;
    }

    @Modified
    void modified(BundleContext context, Map<String, Object> map) {
        if (logger != null) logger.info("Configuration modified; restart the plugin to apply it.");
    }

    @Deactivate
    void deactivate(BundleContext context, Map<String, Object> map) {
        isStopped();
        this.context = null;
        this.map = null;
    }

    @Override
    public boolean isActive() {
        return isActive.get();
    }

    @Override
    public void setIsActive(boolean isActive) {
        this.isActive.set(isActive);
    }

    @Override
    public boolean inMsg(MsgEvent incoming) {
        PluginBuilder pb = pluginBuilder;
        if (pb == null) return false;
        pb.msgIn(incoming);
        return true;
    }

    @Override
    public boolean isStarted() {
        try {
            if (pluginBuilder == null) {
                PluginBuilder pb = new PluginBuilder(this.getClass().getName(), context, map);
                logger = pb.getLogger(Plugin.class.getName(), CLogger.Level.Info);
                executor = new PluginExecutor(pb);
                pb.setExecutor(executor);
                pluginBuilder = pb;

                while (!pb.getAgentService().getAgentState().isActive()) {
                    logger.info("Plugin " + pb.getPluginID() + " waiting on Agent Init");
                    Thread.sleep(1000);
                }

                startWebServer();

                isActive.set(true);
                pb.setIsActive(true);
            }
            return isActive.get();
        } catch (Exception ex) {
            if (logger != null) logger.error("skeletonweb startup failed: " + ex.getMessage(), ex);
            isStopped();
            return false;
        }
    }

    private void startWebServer() throws Exception {
        int port = pluginBuilder.getConfig().getIntegerParam("web_port", DEFAULT_WEB_PORT);
        String host = pluginBuilder.getConfig().getStringParam("web_host");

        ResourceConfig rc = new ResourceConfig()
                .register(NotFoundExceptionHandler.class)
                .register(Example.class);

        ServletContextHandler webContext = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        webContext.setContextPath("/");
        ServletHolder jersey = new ServletHolder(new ServletContainer(rc));
        jersey.setInitOrder(0);
        webContext.addServlet(jersey, "/*");

        jettyServer = new Server();
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendServerVersion(false);   // don't advertise the server version
        ServerConnector connector = new ServerConnector(jettyServer, new HttpConnectionFactory(httpConfig));
        connector.setPort(port);
        if (host != null && !host.isEmpty()) connector.setHost(host);
        jettyServer.addConnector(connector);
        jettyServer.setHandler(webContext);

        // Jersey/HK2 discover their providers through the thread context class loader; inside OSGi that
        // must be this bundle's class loader (where Jetty and Jersey are embedded).
        Thread current = Thread.currentThread();
        ClassLoader previous = current.getContextClassLoader();
        current.setContextClassLoader(Plugin.class.getClassLoader());
        try {
            jettyServer.start();
        } finally {
            current.setContextClassLoader(previous);
        }
        logger.info("skeletonweb listening on " + (host != null && !host.isEmpty() ? host : "*") + ":" + connector.getLocalPort());
    }

    @Override
    public boolean isStopped() {
        isActive.set(false);
        Server server = jettyServer;
        jettyServer = null;
        if (server != null) {
            try {
                server.stop();
            } catch (Exception ex) {
                if (logger != null) logger.error("embedded web server shutdown error: " + ex.getMessage());
            }
        }
        PluginBuilder pb = pluginBuilder;
        if (pb != null) {
            try {
                pb.setExecutor(null);
                pb.setIsActive(false);
            } catch (Exception ignore) {
                // framework already tearing down
            }
        }
        pluginBuilder = null;
        return true;
    }
}
