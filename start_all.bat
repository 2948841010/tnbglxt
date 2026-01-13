@echo off
chcp 65001 >nul
echo ========================================
echo    糖尿病管理系统 - 一键启动脚本
echo ========================================
echo.

:: 启动 Spring Boot 后端 (tlbglxt)
echo [1/5] 启动 Spring Boot 后端...
start "SpringBoot后端" cmd /k "cd /d %~dp0tlbglxt && java -jar target/tlbglxt-0.0.1-SNAPSHOT.jar"
timeout /t 5 /nobreak >nul

:: 启动 MCP 服务器
echo [2/5] 启动 MCP 服务器...
start "MCP服务器" cmd /k "cd /d %~dp0mcp && python server.py"
timeout /t 3 /nobreak >nul

:: 启动 RAG 知识库服务
echo [3/5] 启动 RAG 知识库服务...
start "RAG服务" cmd /k "cd /d %~dp0rag && python retrieval_api.py"
timeout /t 3 /nobreak >nul

:: 启动 Agent 后端
echo [4/5] 启动 Agent 后端...
start "Agent服务" cmd /k "cd /d %~dp0agent && python main.py"
timeout /t 3 /nobreak >nul

:: 启动用户前端
echo [5/5] 启动用户前端...
start "用户前端" cmd /k "cd /d %~dp0fronted && npm run dev"

echo.
echo ========================================
echo    所有服务已启动！
echo ========================================
echo.
echo 服务端口：
echo   - Spring Boot 后端: http://localhost:8080
echo   - MCP 服务器:       http://localhost:50001
echo   - RAG 服务:         http://localhost:8001
echo   - Agent 服务:       http://localhost:8081
echo   - 用户前端:         http://localhost:3000
echo.
echo 按任意键退出此窗口（服务会继续运行）...
pause >nul
