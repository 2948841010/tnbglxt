"""
会话管理器 - 管理用户对话会话和上下文
使用MongoDB作为持久化存储，Redis作为缓存
"""

import json
import logging
from typing import Dict, List, Any, Optional
from datetime import datetime, timedelta
import redis
from config import REDIS_CONFIG
from mongodb_manager import MongoDBManager

logger = logging.getLogger(__name__)

class SessionManager:
    """会话管理器 - MongoDB + Redis双层存储"""
    
    def __init__(self):
        # 初始化MongoDB（主存储）
        try:
            self.mongodb = MongoDBManager()
            logger.info("✅ MongoDB持久化存储已启用")
        except Exception as e:
            logger.error(f"❌ MongoDB初始化失败: {e}")
            raise
        
        # 初始化Redis（缓存层）
        try:
            self.redis_client = redis.Redis(
                host=REDIS_CONFIG['host'],
                port=REDIS_CONFIG['port'],
                db=REDIS_CONFIG['db'],
                decode_responses=True
            )
            # 测试连接
            self.redis_client.ping()
            logger.info("✅ Redis缓存层已启用")
        except Exception as e:
            logger.warning(f"⚠️ Redis连接失败，仅使用MongoDB: {e}")
            self.redis_client = None
        
        # 用户当前会话ID映射（内存缓存）
        self.user_session_map = {}
    
    def _get_cache_key(self, session_id: str) -> str:
        """获取Redis缓存key"""
        return f"ai_session_cache:{session_id}"
    
    def _get_session_from_cache(self, session_id: str) -> Optional[Dict[str, Any]]:
        """从Redis缓存获取会话"""
        if not self.redis_client:
            return None
        
        try:
            cache_key = self._get_cache_key(session_id)
            cached_data = self.redis_client.get(cache_key)
            
            if cached_data:
                logger.debug(f"🎯 从Redis缓存获取会话: {session_id}")
                return json.loads(cached_data)
        except Exception as e:
            logger.warning(f"从缓存获取会话失败: {e}")
        
        return None
    
    def _set_session_to_cache(self, session_id: str, session_data: Dict[str, Any], expire: int = 3600):
        """设置会话到Redis缓存"""
        if not self.redis_client:
            return
        
        try:
            cache_key = self._get_cache_key(session_id)
            # 转换datetime对象为字符串
            session_json = json.dumps(session_data, ensure_ascii=False, default=str)
            self.redis_client.setex(cache_key, expire, session_json)
            logger.debug(f"💾 会话已缓存到Redis: {session_id}")
        except Exception as e:
            logger.warning(f"缓存会话失败: {e}")
    
    def _clear_session_cache(self, session_id: str):
        """清除Redis缓存"""
        if not self.redis_client:
            return
        
        try:
            cache_key = self._get_cache_key(session_id)
            self.redis_client.delete(cache_key)
            logger.debug(f"🗑️ Redis缓存已清除: {session_id}")
        except Exception as e:
            logger.warning(f"清除缓存失败: {e}")
    
    async def get_or_create_session(self, user_id: int, user_info: Dict[str, Any]) -> str:
        """
        获取或创建用户会话
        
        Args:
            user_id: 用户ID
            user_info: 用户信息
            
        Returns:
            会话ID
        """
        # 先从内存映射查找当前会话
        if user_id in self.user_session_map:
            session_id = self.user_session_map[user_id]
            
            # 验证会话是否存在
            session = await self.mongodb.get_session(user_id, session_id)
            if session:
                logger.debug(f"📋 使用现有会话: {session_id}")
                return session_id
        
        # 尝试获取最新会话
        session = await self.mongodb.get_session(user_id)
        
        if session:
            session_id = session["sessionId"]
            self.user_session_map[user_id] = session_id
            logger.debug(f"📋 获取最新会话: {session_id}")
        else:
            # 创建新会话
            session_id = await self.mongodb.create_session(user_id, user_info)
            self.user_session_map[user_id] = session_id
            logger.info(f"🆕 创建新会话: {session_id}")
        
        return session_id
    
    async def create_new_session(self, user_id: int, user_info: Dict[str, Any]) -> str:
        """
        强制创建新会话（不检查现有会话）
        
        Args:
            user_id: 用户ID
            user_info: 用户信息
            
        Returns:
            新会话ID
        """
        # 直接创建新会话，不检查现有会话
        session_id = await self.mongodb.create_session(user_id, user_info)
        
        # 更新内存映射
        self.user_session_map[user_id] = session_id
        
        logger.info(f"🆕 强制创建新会话: {session_id}")
        return session_id
    
    async def set_current_session(self, user_id: int, session_id: str, user_info: Dict[str, Any]) -> bool:
        """
        设置用户的当前会话
        
        Args:
            user_id: 用户ID
            session_id: 会话ID
            user_info: 用户信息（用于创建新会话，如果不存在）
            
        Returns:
            是否成功设置
        """
        try:
            # 验证会话是否存在
            session = await self.mongodb.get_session(user_id, session_id)
            
            if session:
                # 会话存在，直接设置为当前会话
                self.user_session_map[user_id] = session_id
                logger.info(f"✅ 已切换到会话: {session_id}")
                return True
            else:
                # 会话不存在，可能是新创建的会话ID
                # 创建新会话（使用提供的session_id）
                await self.mongodb.create_session(user_id, user_info, session_id=session_id)
                self.user_session_map[user_id] = session_id
                logger.info(f"🆕 创建并切换到新会话: {session_id}")
                return True
                
        except Exception as e:
            logger.error(f"设置当前会话失败: {e}")
            return False
    
    async def get_session(self, user_id: int) -> Dict[str, Any]:
        """
        获取用户会话（兼容旧接口）
        
        Args:
            user_id: 用户ID
            
        Returns:
            会话数据
        """
        # 获取当前会话ID
        session_id = self.user_session_map.get(user_id)
        
        if not session_id:
            # 尝试获取最新会话
            session = await self.mongodb.get_session(user_id)
            if session:
                session_id = session["sessionId"]
                self.user_session_map[user_id] = session_id
                return session
            else:
                # 返回空会话结构
                return {
                    "userId": user_id,
                    "messages": [],
                    "context": {},
                    "statistics": {
                        "totalMessages": 0,
                        "userMessages": 0,
                        "assistantMessages": 0
                    },
                    "createTime": datetime.now().isoformat(),
                    "updateTime": datetime.now().isoformat()
                }
        
        # 先尝试从缓存获取
        cached_session = self._get_session_from_cache(session_id)
        if cached_session:
            return cached_session
        
        # 从MongoDB获取
        session = await self.mongodb.get_session(user_id, session_id)
        
        if session:
            # 缓存到Redis
            self._set_session_to_cache(session_id, session)
            return session
        else:
            # 返回空会话结构
            return {
                "userId": user_id,
                "messages": [],
                "context": {},
                "statistics": {
                    "totalMessages": 0,
                    "userMessages": 0,
                    "assistantMessages": 0
                },
                "createTime": datetime.now().isoformat(),
                "updateTime": datetime.now().isoformat()
            }
    
    async def save_session(self, user_id: int, session_data: Dict[str, Any]):
        """
        保存用户会话（兼容旧接口，但不推荐使用）
        
        Args:
            user_id: 用户ID
            session_data: 会话数据
        """
        logger.warning("⚠️ save_session已废弃，请使用add_message等方法")
        # 此方法保留用于兼容性，但实际存储通过add_message等方法完成
    
    async def add_message(
        self, 
        user_id: int, 
        role: str, 
        content: str, 
        tool_calls: Optional[List[Dict]] = None, 
        thinking: Optional[str] = None, 
        expert_plan: Optional[Dict] = None
    ):
        """
        添加消息到会话
        
        Args:
            user_id: 用户ID
            role: 消息角色 (user, assistant, tool)
            content: 消息内容
            tool_calls: 工具调用信息（可选）
            thinking: AI思考过程（可选）
            expert_plan: 专家计划信息（可选）
        """
        # 获取当前会话ID
        session_id = self.user_session_map.get(user_id)
        
        if not session_id:
            logger.warning(f"⚠️ 用户{user_id}没有活动会话，无法添加消息")
            return
        
        # 构建消息数据
        message_data = {
            "role": role,
            "content": content,
            "timestamp": datetime.now()
        }
        
        if tool_calls:
            message_data["toolCalls"] = tool_calls
            
        if thinking and thinking.strip():
            message_data["thinking"] = thinking.strip()
            logger.info(f"💾 存储thinking字段，长度: {len(thinking.strip())}")
        
        if expert_plan:
            message_data["expertPlan"] = expert_plan
            logger.info(f"💾 存储expertPlan字段，包含 {len(expert_plan.get('experts', []))} 个专家")
        
        # 保存到MongoDB
        success = await self.mongodb.add_message(session_id, message_data)
        
        if success:
            # 清除Redis缓存，下次获取时会从MongoDB重新加载
            self._clear_session_cache(session_id)
            logger.debug(f"✅ 消息已添加到MongoDB: {session_id}")
        else:
            logger.error(f"❌ 添加消息失败: {session_id}")
    
    async def get_conversation_history(self, user_id: int, limit: int = 20) -> List[Dict[str, Any]]:
        """
        获取对话历史
        
        Args:
            user_id: 用户ID
            limit: 返回消息数量限制
            
        Returns:
            对话消息列表
        """
        # 获取当前会话ID
        session_id = self.user_session_map.get(user_id)
        
        # 从MongoDB获取
        messages = await self.mongodb.get_conversation_history(user_id, session_id, limit)
        
        return messages
    
    async def update_context(self, user_id: int, context_key: str, context_value: Any):
        """
        更新会话上下文
        
        Args:
            user_id: 用户ID
            context_key: 上下文键
            context_value: 上下文值
        """
        # 获取当前会话ID
        session_id = self.user_session_map.get(user_id)
        
        if not session_id:
            logger.warning(f"⚠️ 用户{user_id}没有活动会话，无法更新上下文")
            return
        
        # 更新MongoDB
        success = await self.mongodb.update_context(session_id, context_key, context_value)
        
        if success:
            # 清除Redis缓存
            self._clear_session_cache(session_id)
            logger.debug(f"✅ 上下文已更新: {context_key}")
    
    async def clear_session(self, user_id: int):
        """
        清除用户当前会话（删除MongoDB中的会话数据）
        
        Args:
            user_id: 用户ID
        """
        # 获取当前会话ID
        old_session_id = self.user_session_map.pop(user_id, None)
        
        if old_session_id:
            # 清除Redis缓存
            self._clear_session_cache(old_session_id)
            
            # 从MongoDB中删除当前会话
            delete_success = await self.mongodb.delete_session(old_session_id, user_id)
            
            if delete_success:
                logger.info(f"🗑️ 用户{user_id}的会话已从MongoDB中删除: {old_session_id}")
            else:
                logger.warning(f"⚠️ 删除会话失败或会话不存在: {old_session_id}")
        else:
            # 如果内存中没有session_id，尝试获取最新会话并删除
            latest_session = await self.mongodb.get_session(user_id)
            if latest_session:
                session_id = latest_session["sessionId"]
                delete_success = await self.mongodb.delete_session(session_id, user_id)
                if delete_success:
                    logger.info(f"🗑️ 删除了用户{user_id}的最新会话: {session_id}")
                self._clear_session_cache(session_id)
            else:
                logger.info(f"ℹ️ 用户{user_id}没有需要清除的会话")
    
    async def get_user_sessions_list(self, user_id: int, limit: int = 10) -> List[Dict[str, Any]]:
        """
        获取用户的会话列表
        
        Args:
            user_id: 用户ID
            limit: 返回数量限制
            
        Returns:
            会话列表
        """
        return await self.mongodb.get_user_sessions(user_id, limit)
    
    def close(self):
        """关闭连接"""
        if self.mongodb:
            self.mongodb.close()
        if self.redis_client:
            self.redis_client.close()
        logger.info("SessionManager已关闭")
