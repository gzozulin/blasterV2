
# Blaster v2
Hello, and welcome to **Blaster**!
Blaster is a platform for rendering offline and realtime scenes with **Kotlin**.

This project is a natural development of its predecessor, [Blaster v1.](https://github.com/gzozulin/blaster)

The **goals are multiple** - therefore, it is convenient to store various subprojects together: they operate on a shared set of assets and a common codebase.

## Minigl
The realtime graphical library of choice for Blaster is OpenGL with [LWJGL](https://www.lwjgl.org/) wrappers (only window and API bindings). I am not using any existing rendering engine, because staring cluelessly at the black screen is my favorite way to spend time. Also, I want to understand things.

I am trying to keep **minigl** as simple as possible. One reason is that I do not know yet how to make it neat and sophisticated, and the second is that it makes the code much more versatile and less domain-specific.

The internal structure consists of:
1. [/assets/](https://github.com/gzozulin/blasterV2/tree/master/minigl/src/main/kotlin/com/gzozulin/minigl/assets) - resources loading and handling
2. [/gl/](https://github.com/gzozulin/blasterV2/tree/master/minigl/src/main/kotlin/com/gzozulin/minigl/gl)	- common OpenGL API's wrapped into classes
3. [/scene/](https://github.com/gzozulin/blasterV2/tree/master/minigl/src/main/kotlin/com/gzozulin/minigl/scene) - scene elements: cameras, lights, etc.
4. [/techniques/](https://github.com/gzozulin/blasterV2/tree/master/minigl/src/main/kotlin/com/gzozulin/minigl/techniques) - highlevel constructs to facilitate rendering

## Simulator
I always wanted to **create games**. And I did a couple of times and tried many times more. The results are usually pretty shallow - straightforward arcades for mobiles. The problem with game development for me is that I enjoy sleeping. Since most of my free programming happens after working hours, I cannot allocate a year or two of zombie-time to create something a little more involved — sort of a convenience problem.

Now I would like to try a different approach. Recently I watched a couple of videos where the guy was playing [Dwarf Fortress](http://www.bay12games.com/dwarves/). It is a very involved game with elaborate mechanics - the kind of game I would be very hesitant to start programming. And then an idea came: what if I can **approach that iteratively**? One small and independent addition at a time?

I am not bounded by any contracts or restrictions  - it is just **an art form for me**. I can start wherever I want, and I can finish as I am pleased. Since I do not wish to receive any cash from it, I'm also not bound to particular design considerations. I don't have to create one more free-to-play money extracting clone. I'm even **more interested in the simulation** aspect of the game than graphics - similar to Dwarf Fortress.

## Runnables
Each project contains a set of runnables. To try them out, you need to clone the repo and run an appropriate [Gradle](https://docs.gradle.org/current/userguide/application_plugin.html) command. In 99% of the cases, you can control the camera with **WASD** and **mouse look**.

* Don't forget to install [you know what](https://www.oracle.com/ca-en/java/technologies/javase/javase-jdk8-downloads.html) to assemble and run the code
* For Windows users: [this tool](https://gitforwindows.org/) can simplify life without a console

### Soccer
```./gradlew -PmainClass=com.gzozulin.sim.simulation.SoccerKt :simulator:run```

<img align="left" width="200px" src="http://gzozulin.com/wp-content/uploads/2020/07/balls.png" />
<b>Soccer</b> was born to work out ideas of simulation of a big world. The scenes' physics is straightforward but easily scalable: I am simulating the bouncing of 30k of soccer balls on the field. I hope that some of the concepts introduced with this scene will be polished over time, and others will be just replaced. I was primarily interested in trying <href src="https://www.gamasutra.com/view/feature/3355/postmortem_thief_the_dark_project.php?print=1">composition</href> of game objects instead of rigid hierarchy to allow plugin-alike extension of mechanics.

### TileMap
```./gradlew -PmainClass=com.gzozulin.minigl.techniques.TileMapKt :minigl:run```

<img align="left" width="200px" src="http://gzozulin.com/wp-content/uploads/2020/07/tileeeees.png" />
Since I am planning to work mostly myself, the abstraction is fundamental. I believe that the best balance between detail and the amount of effort for the environment can be achieved with <b>tilemaps</b>. I am not yet decided on the particular technique - I might try <href src="https://www.gog.com/game/fallout_2">hex tiles</href> later on. One crucial point is that each tile field - multiple tiles - should be represented by only two triangles. Therefore most of the blending and UV mapping should be handled on the GPU side.

### PBR
```./gradlew -PmainClass=com.gzozulin.minigl.techniques.PhysicsKt :minigl:run```

<img align="left" width="200px" src="http://gzozulin.com/wp-content/uploads/2020/07/mandalorian.png" />
This application is my take on the <b>Physically Based Rendering (PBR).</b> I am not planning to use this technique for the simulation from the start, since the approach is complex and demanding. I want to start with primitive graphics to focus mainly on the simulation aspect. Later on, when I have something already working, I might switch to PBR renderer. The approach itself can be expended to include Environment Lighting and Deferred passes.

### Deferred
```./gradlew -PmainClass=com.gzozulin.minigl.techniques.DeferredKt :minigl:run```

<img align="left" width="200px" src="http://gzozulin.com/wp-content/uploads/2020/07/runhgold1.png" />
Pretty well known <b>Ambient Diffuse Specular (ADS)</b> shading technique with deferred lighting pass. It is not intended to be used now but can be handy to render static geometry for the scene. As with PBR, I might use this technique later on for less exciting parts of the environment. For now, it just allows me to shade 3d objects loaded with Wavefront *.obj format. Its main advantage is that it allows me to use a lot of light sources at a relatively low additional cost.


### Billboards
```./gradlew -PmainClass=com.gzozulin.minigl.techniques.BillboardKt :minigl:run```

<img align="left" width="200px" src="http://gzozulin.com/wp-content/uploads/2020/03/text.png" /> <b>Billboarding</b> is an effortless technique to implement. In its essence, a billboard is just a rectangle, always oriented towards the camera. In my implementation, this rectangle rendered with instancing, and all of the matrix calculations happens on the GPU. Billboards are planned to cover two use cases in my simulation: particle systems and character animations. I want to use pre-baked sprites to render characters from relatively far away. This way, the lack of real-time lighting will be less noticeable, but I will be able to handle many moving actors.

### Skyboxes
```./gradlew -PmainClass=com.gzozulin.minigl.techniques.SkyboxKt :minigl:run```

<img align="left" width="200px" src="http://gzozulin.com/wp-content/uploads/2020/03/skybox.png" />
<b>Skyboxes</b> are, so far, the most significant revelation for me in Computer Graphics. They allow me to add a great deal of depth and perspective to the environment with minimal effort. It is also the fastest way to create a context and setting for the scene. In Blaster, skyboxes are wrapped into easy to use technique class and are used all across the codebase. As the primary source of inspiration and textures, I use a pack of skyboxes from user-created Quake 3 levels.

### Simple
```./gradlew -PmainClass=com.gzozulin.minigl.techniques.SimpleKt :minigl:run```

<img align="left" width="200px" src="http://gzozulin.com/wp-content/uploads/2020/07/simple.png" />
As the name suggests, <b>Simple</b> is a straightforward scene with just a couple of triangles. I developed a liking to start any rendering framework in this manner. Despite its apparent plainness, this setup allows me to engage all of the main components of real-time renderer: shaders, geometry, textures, and matrices operations. When this arrangement is debugged and working, the followup becomes much more manageable.

