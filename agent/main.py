"""
Agent后端主应用
"""

import logging
import uvicorn
import json
from fastapi import FastAPI, HTTPException, Security, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
from datetime import datetime

from agent_service import AgentService
from config import AGENT_HOST, AGENT_PORT, DEBUG
from jwt_auth import get_current_user, get_current_user_optional

# 配置日志
logging.basicConfig(
    level=logging.INFO if not DEBUG else logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 创建FastAPI应用
app = FastAPI(
    title="智能问诊Agent后端",
    description="为糖尿病患者提供智能咨询服务的Agent后端",
    version="1.0.0"
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://127.0.0.1:3000"],  # 明确允许前端域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 初始化Agent服务
agent_service = AgentService()

# 请求/响应模型
class ChatRequest(BaseModel):
    message: str

class ChatResponse(BaseModel):
    success: bool
    response: Optional[str] = None
    tool_calls: Optional[List[Dict[str, Any]]] = None
    usage: Optional[Dict[str, Any]] = None
    error: Optional[str] = None

class HistoryResponse(BaseModel):
    success: bool
    user_info: Optional[Dict[str, Any]] = None
    messages: Optional[List[Dict[str, Any]]] = None
    total_messages: Optional[int] = None
    error: Optional[str] = None

class ClearResponse(BaseModel):
    success: bool
    message: Optional[str] = None
    error: Optional[str] = None

class SessionListResponse(BaseModel):
    success: bool
    sessions: Optional[List[Dict[str, Any]]] = None
    current_session_id: Optional[str] = None
    total_count: Optional[int] = None
    error: Optional[str] = None

class SwitchSessionRequest(BaseModel):
    session_id: str

class SwitchSessionResponse(BaseModel):
    success: bool
    session_id: Optional[str] = None
    messages: Optional[List[Dict[str, Any]]] = None
    message: Optional[str] = None
    error: Optional[str] = None

class NewSessionResponse(BaseModel):
    success: bool
    session_id: Optional[str] = None
    message: Optional[str] = None
    error: Optional[str] = None

@app.on_event("startup")
async def startup_event():
    """应用启动事件"""
    logger.info("Agent后端服务启动中...")

@app.on_event("shutdown") 
async def shutdown_event():
    """应用关闭事件"""
    logger.info("Agent后端服务关闭中...")

@app.get("/")
async def root():
    """根路径 - 健康检查"""
    return {
        "service": "智能问诊Agent后端",
        "status": "running",
        "timestamp": datetime.now().isoformat(),
        "version": "1.0.0"
    }

@app.get("/health")
async def health_check():
    """健康检查接口"""
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "services": {
            "deepseek_api": "connected",
            "mcp_tools": "available",
            "session_manager": "active"
        }
    }

@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest, current_user: Dict[str, Any] = Security(get_current_user)):
    """智能对话接口（需要JWT认证）"""
    try:
        user_id = current_user["user_id"]
        logger.info(f"收到用户{user_id}的消息: {request.message[:100]}...")
        
        # 处理消息
        result = await agent_service.process_message(user_id, request.message)
        
        if result["success"]:
            return ChatResponse(
                success=True,
                response=result["response"],
                tool_calls=result.get("tool_calls", []),
                usage=result.get("usage", {})
            )
        else:
            raise HTTPException(status_code=500, detail=result["error"])
            
    except Exception as e:
        logger.error(f"对话处理失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/chat/history", response_model=HistoryResponse)
async def get_chat_history(limit: int = 20, current_user: Dict[str, Any] = Security(get_current_user)):
    """获取对话历史（需要JWT认证）"""
    try:
        user_id = current_user["user_id"]
        result = await agent_service.get_conversation_history(user_id, limit)
        
        if result["success"]:
            return HistoryResponse(
                success=True,
                user_info=result["user_info"],
                messages=result["messages"],
                total_messages=result["total_messages"]
            )
        else:
            raise HTTPException(status_code=404, detail=result["error"])
            
    except Exception as e:
        logger.error(f"获取对话历史失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/chat/history", response_model=ClearResponse)
async def clear_chat_history(current_user: Dict[str, Any] = Security(get_current_user)):
    """清除对话历史（需要JWT认证）"""
    try:
        user_id = current_user["user_id"]
        result = await agent_service.clear_conversation(user_id)
        
        if result["success"]:
            return ClearResponse(
                success=True,
                message=result["message"]
            )
        else:
            raise HTTPException(status_code=500, detail=result["error"])
            
    except Exception as e:
        logger.error(f"清除对话历史失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/chat/sessions", response_model=SessionListResponse)
async def get_chat_sessions(limit: int = 10, current_user: Dict[str, Any] = Security(get_current_user)):
    """获取用户的会话列表（需要JWT认证）"""
    try:
        user_id = current_user["user_id"]
        
        # 获取会话列表
        sessions = await agent_service.session_manager.get_user_sessions_list(user_id, limit)
        
        # 获取当前会话ID
        current_session_id = agent_service.session_manager.user_session_map.get(user_id)
        
        # 格式化会话信息
        formatted_sessions = []
        for session in sessions:
            messages = session.get("messages", [])
            last_message = messages[-1] if messages else None
            
            formatted_session = {
                "sessionId": session.get("sessionId"),
                "createTime": session.get("createTime"),
                "updateTime": session.get("updateTime"),
                "messageCount": session.get("statistics", {}).get("totalMessages", 0),
                "lastMessage": {
                    "role": last_message.get("role") if last_message else None,
                    "content": last_message.get("content", "")[:50] + "..." if last_message and last_message.get("content") else None
                } if last_message else None
            }
            formatted_sessions.append(formatted_session)
        
        return SessionListResponse(
            success=True,
            sessions=formatted_sessions,
            current_session_id=current_session_id,
            total_count=len(formatted_sessions)
        )
        
    except Exception as e:
        logger.error(f"获取会话列表失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/chat/sessions/switch", response_model=SwitchSessionResponse)
async def switch_chat_session(request: SwitchSessionRequest, current_user: Dict[str, Any] = Security(get_current_user)):
    """切换到指定会话（需要JWT认证）"""
    try:
        user_id = current_user["user_id"]
        session_id = request.session_id
        
        # 验证会话是否存在且属于当前用户
        session = await agent_service.session_manager.mongodb.get_session(user_id, session_id)
        
        if not session:
            raise HTTPException(status_code=404, detail="会话不存在或无权访问")
        
        # 更新当前会话ID
        agent_service.session_manager.user_session_map[user_id] = session_id
        
        # 获取会话的对话历史
        messages = await agent_service.session_manager.get_conversation_history(user_id, limit=50)
        
        # 转换字段名格式
        formatted_messages = []
        for msg in messages:
            formatted_msg = {
                "role": msg.get("role"),
                "content": msg.get("content"),
                "timestamp": msg.get("timestamp")
            }
            
            if "toolCalls" in msg:
                formatted_msg["tool_calls"] = msg["toolCalls"]
            if "thinking" in msg:
                formatted_msg["thinking"] = msg["thinking"]
            if "expertPlan" in msg:
                formatted_msg["expert_plan"] = msg["expertPlan"]
            
            formatted_messages.append(formatted_msg)
        
        return SwitchSessionResponse(
            success=True,
            session_id=session_id,
            messages=formatted_messages,
            message=f"已切换到会话 {session_id}"
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"切换会话失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/chat/sessions/new", response_model=NewSessionResponse)
async def create_new_session(current_user: Dict[str, Any] = Security(get_current_user)):
    """创建新会话（需要JWT认证）"""
    try:
        user_id = current_user["user_id"]
        user_info = await agent_service.get_user_info(user_id)
        
        if not user_info:
            raise HTTPException(status_code=404, detail="用户不存在")
        
        # 强制创建新会话
        new_session_id = await agent_service.session_manager.create_new_session(user_id, user_info)
        
        logger.info(f"✅ 用户 {user_id} 创建新会话: {new_session_id}")
        
        return NewSessionResponse(
            success=True,
            session_id=new_session_id,
            message="新会话已创建"
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"创建新会话失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/tools")
async def get_available_tools(current_user: Optional[Dict[str, Any]] = Security(get_current_user_optional)):
    """获取可用工具列表（可选认证）"""
    try:
        tool_schemas = agent_service.mcp_client.get_tool_schemas()
        result = {
            "success": True,
            "tools": tool_schemas,
            "total_tools": len(tool_schemas)
        }
        
        # 如果用户已认证，添加用户信息
        if current_user:
            result["authenticated_user"] = {
                "user_id": current_user["user_id"],
                "username": current_user["username"],
                "user_type": current_user["user_type"]
            }
        
        return result
    except Exception as e:
        logger.error(f"获取工具列表失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/tools/call")
async def call_tool_directly(tool_name: str, parameters: Dict[str, Any]):
    """直接调用MCP工具"""
    try:
        result = await agent_service.mcp_client.call_tool(tool_name, parameters)
        return result
    except Exception as e:
        logger.error(f"直接调用工具失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/user/info")
async def get_user_info_endpoint(current_user: Dict[str, Any] = Security(get_current_user)):
    """获取当前用户信息接口（需要JWT认证）"""
    try:
        user_id = current_user["user_id"]
        user_info = await agent_service.get_user_info(user_id)
        if user_info:
            return {
                "success": True,
                "user_info": user_info
            }
        else:
            raise HTTPException(status_code=404, detail="用户不存在")
    except Exception as e:
        logger.error(f"获取用户信息失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/auth/verify")
async def verify_token(current_user: Dict[str, Any] = Security(get_current_user)):
    """验证JWT token是否有效"""
    return {
        "success": True,
        "message": "Token验证成功",
        "user_info": {
            "user_id": current_user["user_id"],
            "username": current_user["username"],
            "user_type": current_user["user_type"]
        }
    }

@app.get("/auth/token-info")
async def get_token_info(current_user: Dict[str, Any] = Security(get_current_user)):
    """获取当前token的详细信息"""
    from jwt_auth import jwt_auth
    
    token = current_user["token"]
    token_info = jwt_auth.get_token_info(token)
    
    return {
        "success": True,
        "token_info": token_info,
        "online_status": jwt_auth.check_user_online_status(current_user["user_id"])
    }

@app.websocket("/ws/chat")
async def websocket_chat(websocket: WebSocket):
    """WebSocket流式聊天接口"""
    await websocket.accept()
    logger.info("WebSocket连接已建立")
    
    try:
        while True:
            # 接收客户端消息
            data = await websocket.receive_text()
            message_data = json.loads(data)
            
            # 验证消息格式
            if "message" not in message_data or "token" not in message_data:
                await websocket.send_text(json.dumps({
                    "type": "error",
                    "error": "缺少必要字段：message 或 token"
                }))
                continue
            
            # 获取session_id（如果有）
            session_id = message_data.get("session_id")
            
            # 验证JWT token
            try:
                from jwt_auth import jwt_auth
                token = message_data["token"]
                
                # 验证token是否有效
                if not jwt_auth.validate_token(token):
                    await websocket.send_text(json.dumps({
                        "type": "error", 
                        "error": "Token无效或已过期"
                    }))
                    continue
                
                # 获取用户ID
                user_id = jwt_auth.get_user_id_from_token(token)
                if user_id is None:
                    await websocket.send_text(json.dumps({
                        "type": "error", 
                        "error": "无法从Token中获取用户信息"
                    }))
                    continue
                    
            except Exception as e:
                await websocket.send_text(json.dumps({
                    "type": "error", 
                    "error": f"Token验证失败: {str(e)}"
                }))
                continue
            
            user_message = message_data["message"]
            logger.info(f"收到用户{user_id}的WebSocket消息: {user_message[:100]}... (会话ID: {session_id})")
            
            # 发送开始响应消息
            await websocket.send_text(json.dumps({
                "type": "start",
                "message": "开始处理您的消息..."
            }))
            
            try:
                # 使用多专家模式流式处理消息
                if agent_service.use_expert_mode:
                    logger.info("🤖 使用多专家模式处理消息")
                    async for chunk in agent_service.process_message_expert_stream(
                        user_id, user_message, session_id=session_id
                    ):
                        await websocket.send_text(json.dumps(chunk))
                else:
                    logger.info("📝 使用标准模式处理消息")
                    async for chunk in agent_service.process_message_stream(
                        user_id, user_message, session_id=session_id
                    ):
                        await websocket.send_text(json.dumps(chunk))
                    
                    # 发送完成信号（多专家模式会自己发送）
                    await websocket.send_text(json.dumps({
                        "type": "complete"
                    }))
                
            except Exception as e:
                logger.error(f"处理消息时出错: {e}")
                await websocket.send_text(json.dumps({
                    "type": "error",
                    "error": f"处理消息失败: {str(e)}"
                }))
    
    except WebSocketDisconnect:
        logger.info("WebSocket连接已断开")
    except Exception as e:
        logger.error(f"WebSocket错误: {e}")
        await websocket.close()

if __name__ == "__main__":
    logger.info(f"启动Agent后端服务: {AGENT_HOST}:{AGENT_PORT}")
    uvicorn.run(
        "main:app",
        host=AGENT_HOST,
        port=AGENT_PORT,
        reload=DEBUG,
        log_level="info" if not DEBUG else "debug"
    ) 