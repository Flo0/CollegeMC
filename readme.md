# CollegeMC

Multi module project of CollegeMC containing
- Common code
- Minecraft common code
- Velocity common code
- Minecraft lobby implementation
- Minecraft primary server implementation

---
## Quick start

Run the `paper-nms:init` maven lifecycle to create remapped nms sources.
This has to be done only once and when updating to a new
Minecraft version.

The project can be build by running `package` or `install` on either the
parent or each child module respectively.

---
## Dependency tree

- Common depends on only external dependencies
- CollegeMC-Core depends on Common and MC dependencies
- Velocity-Core depends on Common and Velocity dependencies
- CollegeMC-Lobby depends on CollegeMC-Core
- CollegeMC-Primary depends on CollegeMC-Core

![Dependency Tree](documentation/dependencies.png)