"""
初始化MongoDB集合和索引
"""

from pymongo import MongoClient, ASCENDING, DESCENDING
from config import MONGODB_CONFIG
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def init_mongodb():
    """初始化MongoDB数据库和集合"""
    try:
        # 连接MongoDB
        if MONGODB_CONFIG.get('username') and MONGODB_CONFIG.get('password'):
            uri = f"mongodb://{MONGODB_CONFIG['username']}:{MONGODB_CONFIG['password']}@{MONGODB_CONFIG['host']}:{MONGODB_CONFIG['port']}/{MONGODB_CONFIG['database']}?authSource=admin"
        else:
            uri = f"mongodb://{MONGODB_CONFIG['host']}:{MONGODB_CONFIG['port']}/{MONGODB_CONFIG['database']}"
        
        client = MongoClient(uri, serverSelectionTimeoutMS=5000)
        
        # 测试连接
        client.admin.command('ping')
        logger.info(f"✅ 连接到MongoDB: {MONGODB_CONFIG['database']}")
        
        db = client[MONGODB_CONFIG['database']]
        
        # 创建AI咨询记录集合
        collection_name = 'ai_consultation_history'
        
        # 检查集合是否存在
        if collection_name in db.list_collection_names():
            logger.info(f"⚠️ 集合 {collection_name} 已存在")
            
            # 询问是否要重新创建索引
            response = input("是否重新创建索引? (y/n): ")
            if response.lower() != 'y':
                logger.info("跳过索引创建")
                client.close()
                return
        else:
            # 创建集合
            db.create_collection(collection_name)
            logger.info(f"✅ 创建集合: {collection_name}")
        
        collection = db[collection_name]
        
        # 删除所有现有索引（除了_id）
        logger.info("清理旧索引...")
        collection.drop_indexes()
        
        # 创建索引
        indexes = [
            {
                'keys': [("userId", ASCENDING), ("createTime", DESCENDING)],
                'name': "idx_userId_createTime"
            },
            {
                'keys': [("userId", ASCENDING), ("sessionId", ASCENDING)],
                'name': "idx_userId_sessionId"
            },
            {
                'keys': [("sessionId", ASCENDING)],
                'name': "idx_sessionId_unique",
                'unique': True
            },
            {
                'keys': [("createTime", DESCENDING)],
                'name': "idx_createTime"
            },
            {
                'keys': [("updateTime", DESCENDING)],
                'name': "idx_updateTime"
            }
        ]
        
        logger.info("创建索引...")
        for index_spec in indexes:
            keys = index_spec.pop('keys')
            collection.create_index(keys, **index_spec)
            logger.info(f"  ✅ 索引创建成功: {index_spec['name']}")
        
        # 验证索引
        logger.info("\n索引列表:")
        for index in collection.list_indexes():
            logger.info(f"  - {index['name']}: {index.get('key', {})}")
        
        # 显示集合统计
        stats = db.command("collstats", collection_name)
        logger.info(f"\n集合统计:")
        logger.info(f"  - 文档数: {stats['count']}")
        logger.info(f"  - 存储大小: {stats['size']} bytes")
        logger.info(f"  - 索引数: {stats['nindexes']}")
        
        logger.info("\n✅ MongoDB初始化完成!")
        logger.info(f"\n数据库: {MONGODB_CONFIG['database']}")
        logger.info(f"集合: {collection_name}")
        logger.info(f"索引数: {len(indexes)}")
        
        # 打印数据结构说明
        print("\n" + "="*60)
        print("AI咨询记录数据结构:")
        print("="*60)
        print("""
{
  "sessionId": "ai_session_20240115_user3_xxxxx",
  "userId": 3,
  "userInfo": {
    "username": "test_user",
    "realName": "张三",
    "gender": "男"
  },
  "messages": [
    {
      "messageId": "msg_001",
      "role": "user/assistant",
      "content": "消息内容",
      "timestamp": ISODate("2024-01-15T10:00:00Z"),
      "thinking": "AI思考过程",
      "toolCalls": [],
      "expertPlan": {}
    }
  ],
  "context": {},
  "statistics": {
    "totalMessages": 0,
    "userMessages": 0,
    "assistantMessages": 0,
    "expertExecutions": 0,
    "mcpCalls": 0
  },
  "createTime": ISODate("2024-01-15T10:00:00Z"),
  "updateTime": ISODate("2024-01-15T10:30:00Z"),
  "lastAccessTime": ISODate("2024-01-15T10:30:00Z")
}
        """)
        print("="*60)
        
        client.close()
        
    except Exception as e:
        logger.error(f"❌ 初始化失败: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    print("\n" + "="*60)
    print("MongoDB AI咨询集合初始化工具")
    print("="*60 + "\n")
    
    print(f"数据库: {MONGODB_CONFIG['database']}")
    print(f"主机: {MONGODB_CONFIG['host']}:{MONGODB_CONFIG['port']}")
    print(f"集合: ai_consultation_history\n")
    
    response = input("确认初始化? (y/n): ")
    if response.lower() == 'y':
        init_mongodb()
    else:
        print("已取消")

