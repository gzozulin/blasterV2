
# Blaster v2
Hello, and welcome to **Blaster**!
Blaster is a platform for rendering offline and realtime scenes with **Kotlin**.

This project is a natural development of its predecessor, [Blaster v1.](https://github.com/gzozulin/blaster)

The **goals are multiple** - therefore, it is convenient to store various subprojects together: they operate on a shared set of assets and a common codebase.

### Minigl
The realtime graphical library of choice for Blaster is OpenGL with [LWJGL](https://www.lwjgl.org/) wrappers (only window and API bindings). I am not using any existing rendering engine, because staring cluelessly at the black screen is my favorite way to spend time. Also, I want to understand things.

I am trying to keep **minigl** as simple as possible. One reason is that I do not know yet how to make it neat and sophisticated, and the second is that it makes the code much more versatile and less domain-specific.

The internal structure consists of:
1. [/assets/](https://github.com/gzozulin/blasterV2/tree/master/minigl/src/main/kotlin/com/gzozulin/minigl/assets) - resources loading and handling
2. [/gl/](https://github.com/gzozulin/blasterV2/tree/master/minigl/src/main/kotlin/com/gzozulin/minigl/gl)	- common OpenGL API's wrapped into classes
3. [/scene/](https://github.com/gzozulin/blasterV2/tree/master/minigl/src/main/kotlin/com/gzozulin/minigl/scene) - scene elements: cameras, lights, etc.
4. [/techniques/](https://github.com/gzozulin/blasterV2/tree/master/minigl/src/main/kotlin/com/gzozulin/minigl/techniques) - highlevel constructs to facilitate rendering

### Simulator
I always wanted to **create games**. And I did a couple of times and tried many times more. The results are usually pretty shallow - straightforward arcades for mobiles. The problem with game development for me is that I enjoy sleeping. Since most of my free programming happens after working hours, I cannot allocate a year or two of zombie-time to create something a little more involved — sort of a convenience problem.

Now I would like to try a different approach. Recently I watched a couple of videos where the guy was playing [Dwarf Fortress]([http://www.bay12games.com/dwarves/](http://www.bay12games.com/dwarves/)). It is a very involved game with elaborate mechanics - the kind of game I would be very hesitant to start programming. And then an idea came: what if I can **approach that iteratively**? One small and independent addition at a time?

I am not bounded by any contracts or restrictions  - it is just **an art form for me**. I can start wherever I want, and I can finish as I am pleased. Since I do not wish to receive any cash from it, I'm also not bound to particular design considerations. I don't have to create one more free-to-play money extracting clone. I'm even **more interested in the simulation** aspect of the game than graphics - similar to Dwarf Fortress.