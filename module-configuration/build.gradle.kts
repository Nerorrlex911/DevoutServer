dependencies {
    //config
    compileOnly("org.yaml:snakeyaml:2.2")
    compileOnly("com.typesafe:config:1.4.3")
    compileOnly("com.electronwill.night-config:core:3.6.7")
    compileOnly("com.electronwill.night-config:toml:3.6.7")
    compileOnly("com.electronwill.night-config:json:3.6.7")
    compileOnly("com.electronwill.night-config:hocon:3.6.7")
    implementation("com.electronwill.night-config:core-conversion:6.0.0")
    compileOnly(project(":server"))
}