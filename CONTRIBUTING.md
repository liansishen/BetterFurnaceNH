# Development and release workflow / 开发与发布流程

## 中文

1. 从最新的 `master` 创建功能或修复分支，在分支上开发并提交。
2. 推送分支，创建以 `master` 为目标的 Pull Request（PR），等待 `Build and test` CI 成功；检查包括 Gradle 构建、Spotless、Checkstyle 和服务端启动。
3. CI 通过后合并 PR，等待合并提交在 `master` 上的 `Build and test` 再次成功。
4. 在该合并提交上创建并推送无 `v` 前缀的三段版本标签，例如 `0.2.3`。
5. 标签触发 `Release tagged build`。发布前会检查版本格式、提交已由 PR 合并进 `master`、该提交最新一次主分支 CI 成功，随后发布正式、开发和源码 JAR。
6. JAR 发布完成后，`Publish Modernity resource pack` 将 `Modernity-BetterFurnaceNH-<版本>.zip` 上传到同一个 GitHub Release，并检查四个产物均存在。
7. 确认发布工作流成功、Release 和四个产物可下载后，完成发布。

发布说明随 PR 放入 `.changelogs/<版本>.md`，供标签工作流读取。已发布版本保留原有标签和产物，后续修复使用新的版本号。

本地验证：`./gradlew build --console=plain`，Windows 使用 `./gradlew.bat build --console=plain`。`build` 会同时打包 Modernity 适配材质包。可单独运行 `packageResourcePacks`，并用 `-PresourcePackVersion=<版本>` 指定 ZIP 文件名中的版本；材质规范见 [resourcepacks/README.md](resourcepacks/README.md)。

starter/migration 是开发模板，可在 GitHub Actions 手动运行 `Export starter and migration templates` 导出为工作流附件。

## English

1. Create a feature or fix branch from the latest `master` and commit your changes.
2. Push the branch and open a pull request targeting `master`. Wait for `Build and test` CI to succeed; it covers the Gradle build, Spotless, Checkstyle and server startup.
3. Merge the PR after CI passes, then wait for the merge commit's `master` CI to succeed.
4. Create and push a bare three-part version tag, such as `0.2.3`, on that merge commit.
5. `Release tagged build` checks the version format, the merged PR and the successful master CI before publishing the regular, development and sources JARs.
6. `Publish Modernity resource pack` then uploads `Modernity-BetterFurnaceNH-<version>.zip` to the same GitHub Release and verifies all four assets are present.
7. Confirm the release workflow succeeded and the release and all four downloads are available.

Include release notes in the PR at `.changelogs/<version>.md`. Preserve published tags and artifacts, and use a new version for subsequent fixes.

Use `./gradlew build --console=plain` for local verification (`./gradlew.bat` on Windows). The build also packages Modernity. Run `packageResourcePacks` alone to package it, optionally setting `-PresourcePackVersion=<version>`; see [resourcepacks/README.md](resourcepacks/README.md).

Starter and migration templates are available as workflow artifacts through the manual `Export starter and migration templates` action.
