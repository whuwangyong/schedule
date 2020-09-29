这是一个基于`SpringBoot` 写的调度框架，通过调用服务器上的脚本，启动/停止/监测服务器上运行的程序。

具备以下功能点：

- 在前端页面添加 job，配置 job 之间的依赖关系、启动/停止/监测脚本
- 具有依赖关系的 job ，能断点续作
- 查询后台数据库
- 页面自动刷新（js）
- `BootStrap` 导航栏、模态框
- `Thymeleaf`
- `MyBatis`，通用 `Mapper`
- `PageHelper` 分页



# run

将resource目录下的数据导入到数据库，即可运行。