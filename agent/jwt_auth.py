"""
JWT认证模块 - 参考Java后端的JWT实现
"""

import jwt
import redis
import logging
from datetime import datetime, timedelta
from typing import Optional, Dict, Any
from fastapi import HTTPException, Security
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from config import REDIS_CONFIG

logger = logging.getLogger(__name__)

# JWT配置 - 与Java后端保持一致
JWT_SECRET = "tlbglxtSecretKeyForJwtTokenGenerationAndValidation2024"
JWT_ALGORITHM = "HS384"  # 修正为HS384算法

# 创建HTTPBearer实例
security = HTTPBearer()

class JWTAuth:
    """JWT认证类"""
    
    def __init__(self):
        try:
            self.redis_client = redis.Redis(
                host=REDIS_CONFIG['host'],
                port=REDIS_CONFIG['port'],
                db=REDIS_CONFIG['db'],
                decode_responses=True
            )
            self.redis_client.ping()
            logger.info("JWT认证模块 - Redis连接成功")
        except Exception as e:
            logger.warning(f"JWT认证模块 - Redis连接失败: {e}")
            self.redis_client = None
    
    def validate_token(self, token: str) -> bool:
        """
        验证JWT token是否有效
        
        Args:
            token: JWT token
            
        Returns:
            是否有效
        """
        try:
            # 解析JWT token
            payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
            
            # 检查是否过期
            exp = payload.get('exp')
            if exp and datetime.fromtimestamp(exp) < datetime.now():
                return False
            
            return True
            
        except jwt.ExpiredSignatureError:
            logger.debug("JWT token已过期")
            return False
        except jwt.InvalidTokenError:
            logger.debug("JWT token无效")
            return False
        except Exception as e:
            logger.error(f"JWT token验证失败: {e}")
            return False
    
    def get_user_id_from_token(self, token: str) -> Optional[int]:
        """
        从JWT token中获取用户ID
        
        Args:
            token: JWT token
            
        Returns:
            用户ID，如果解析失败返回None
        """
        try:
            payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
            user_id = payload.get('userId')
            
            if user_id is not None:
                return int(user_id)
            
            return None
            
        except Exception as e:
            logger.error(f"从JWT token获取用户ID失败: {e}")
            return None
    
    def get_username_from_token(self, token: str) -> Optional[str]:
        """
        从JWT token中获取用户名
        
        Args:
            token: JWT token
            
        Returns:
            用户名，如果解析失败返回None
        """
        try:
            payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
            return payload.get('sub')  # subject字段通常存储用户名
            
        except Exception as e:
            logger.error(f"从JWT token获取用户名失败: {e}")
            return None
    
    def get_user_type_from_token(self, token: str) -> Optional[int]:
        """
        从JWT token中获取用户类型
        
        Args:
            token: JWT token
            
        Returns:
            用户类型，如果解析失败返回None
        """
        try:
            payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
            user_type = payload.get('userType')
            
            if user_type is not None:
                return int(user_type)
            
            return None
            
        except Exception as e:
            logger.error(f"从JWT token获取用户类型失败: {e}")
            return None
    
    def check_user_online_status(self, user_id: int) -> bool:
        """
        检查用户在线状态（从Redis）
        
        Args:
            user_id: 用户ID
            
        Returns:
            是否在线
        """
        if not self.redis_client:
            return True  # Redis不可用时默认认为用户在线
        
        try:
            online_key = f"user:online:{user_id}"
            return self.redis_client.exists(online_key) > 0
            
        except Exception as e:
            logger.error(f"检查用户在线状态失败: {e}")
            return True  # 检查失败时默认认为用户在线
    
    def get_token_info(self, token: str) -> Dict[str, Any]:
        """
        获取token的完整信息
        
        Args:
            token: JWT token
            
        Returns:
            token信息字典
        """
        try:
            payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
            
            return {
                "user_id": payload.get('userId'),
                "username": payload.get('sub'),
                "user_type": payload.get('userType'),
                "issued_at": datetime.fromtimestamp(payload.get('iat', 0)).isoformat() if payload.get('iat') else None,
                "expires_at": datetime.fromtimestamp(payload.get('exp', 0)).isoformat() if payload.get('exp') else None,
                "valid": True
            }
            
        except Exception as e:
            logger.error(f"获取token信息失败: {e}")
            return {"valid": False, "error": str(e)}

# 创建JWT认证实例
jwt_auth = JWTAuth()

# 依赖注入函数
async def get_current_user(credentials: HTTPAuthorizationCredentials = Security(security)) -> Dict[str, Any]:
    """
    获取当前认证用户信息
    
    Args:
        credentials: HTTP Bearer认证凭据
        
    Returns:
        用户信息字典
        
    Raises:
        HTTPException: 认证失败时抛出异常
    """
    token = credentials.credentials
    
    # 验证token
    if not jwt_auth.validate_token(token):
        raise HTTPException(
            status_code=401,
            detail="Invalid or expired token",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    # 获取用户信息
    user_id = jwt_auth.get_user_id_from_token(token)
    username = jwt_auth.get_username_from_token(token)
    user_type = jwt_auth.get_user_type_from_token(token)
    
    if user_id is None:
        raise HTTPException(
            status_code=401,
            detail="Invalid token payload",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    # 检查用户在线状态
    if not jwt_auth.check_user_online_status(user_id):
        raise HTTPException(
            status_code=401,
            detail="User session expired",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    return {
        "user_id": user_id,
        "username": username,
        "user_type": user_type,
        "token": token
    }

async def get_current_user_optional(credentials: Optional[HTTPAuthorizationCredentials] = Security(security)) -> Optional[Dict[str, Any]]:
    """
    获取当前用户信息（可选，不强制要求认证）
    
    Args:
        credentials: HTTP Bearer认证凭据
        
    Returns:
        用户信息字典或None
    """
    if not credentials:
        return None
    
    try:
        return await get_current_user(credentials)
    except HTTPException:
        return None 