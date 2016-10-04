package ncei.onestop.api

import org.elasticsearch.Version
import org.elasticsearch.env.Environment
import org.elasticsearch.node.Node
import org.elasticsearch.plugins.Plugin

class IntegrationTestNode extends Node {

  private Version version
  private Collection<Class<? extends Plugin>> plugins

  public IntegrationTestNode(Environment env, Version version, Collection<Class<? extends Plugin>> classpathPlugins) {
    super(env, version, classpathPlugins)
    this.version = version
    this.plugins = classpathPlugins
  }

  public Collection<Class<? extends Plugin>> getPlugins() {
    return plugins
  }

  public Version getVersion() {
    return version
  }
}
