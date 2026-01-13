<template>
  <div class="ai-consultation">
    <div class="ai-consultation-content" :class="{ 'has-messages': messages.length > 0 }">
      <!-- 空状态：现代化设计 -->
      <div v-if="messages.length === 0" class="empty-state">
        <!-- 历史记录按钮（空状态） -->
        <div class="empty-history-btn">
          <el-dropdown trigger="click" @command="handleSessionCommand" v-if="sessionList.length > 0">
            <el-button text circle>
              <el-icon :size="20"><Clock /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <span style="color: #9ca3af; font-size: 12px;">历史会话</span>
                </el-dropdown-item>
                <el-dropdown-item 
                  v-for="session in sessionList" 
                  :key="session.sessionId"
                  :command="`switch:${session.sessionId}`"
                >
                  <div class="session-item">
                    <div class="session-info">
                      <div class="session-preview">
                        {{ session.lastMessage?.content || '新对话' }}
                      </div>
                      <div class="session-meta">
                        <span class="session-time">{{ formatSessionTime(session.updateTime) }}</span>
                        <span class="session-count">{{ session.messageCount }}条消息</span>
                      </div>
                    </div>
                  </div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        
        <div class="empty-content">
          <!-- 个性化问候 -->
          <div class="brand-header">
            <h1 class="brand-title">{{ greetingMessage }}</h1>
          </div>
          
          <!-- 输入框区域 -->
          <div class="empty-input-section">
            <!-- 免责声明 -->
            <div class="empty-disclaimer">
              <el-icon><Warning /></el-icon>
              <span>AI医疗助手仅供参考，不能替代专业医生诊断</span>
            </div>
            
            <!-- 输入框 -->
            <div class="input-wrapper">
              <!-- 左侧功能按钮 -->
              <div class="input-left-actions">
                <el-tooltip content="附件" placement="top">
                  <el-button text circle size="small">
                    <el-icon><Paperclip /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
              
              <el-input
                v-model="currentMessage"
                type="textarea"
                :autosize="{ minRows: 1, maxRows: 1 }"
                placeholder="发消息或输入 / 选择技能"
                maxlength="500"
                :show-word-limit="false"
                :disabled="isLoading"
                @keydown.ctrl.enter="sendMessage"
                @keydown.enter.exact.prevent="sendMessage"
                class="message-input"
              />
              
              <!-- 右侧功能按钮 -->
              <div class="input-right-actions">
                <el-button text size="small" class="depth-think-btn">
                  <el-icon><MagicStick /></el-icon>
                  深度思考
                </el-button>
              </div>
              
              <transition name="fade-scale">
                <div class="input-send-btn" v-if="currentMessage.trim() || isLoading">
                  <el-button 
                    type="primary" 
                    :loading="isLoading"
                    @click="sendMessage"
                    circle
                    :icon="isLoading ? undefined : ChatDotRound"
                  />
                </div>
              </transition>
            </div>
            
            <!-- 功能标签 -->
            <div class="feature-tags">
              <div class="feature-tag" @click="currentMessage = '我感觉我得了糖尿病，你能帮我诊断一下吗'">
                <el-icon><ChatLineSquare /></el-icon>
                <span>健康咨询</span>
              </div>
              <div class="feature-tag" @click="currentMessage = '分析我的血糖数据'">
                <el-icon><TrendCharts /></el-icon>
                <span>数据分析</span>
              </div>
              <div class="feature-tag" @click="currentMessage = '糖尿病饮食建议'">
                <el-icon><Food /></el-icon>
                <span>饮食建议</span>
              </div>
              <div class="feature-tag" @click="currentMessage = '推荐内分泌科医生'">
                <el-icon><User /></el-icon>
                <span>医生推荐</span>
              </div>
              <div class="feature-tag">
                <el-icon><More /></el-icon>
                <span>更多</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 对话状态：显示消息列表 -->
      <div v-else class="chat-container" ref="chatContainer">
        <!-- 浮动会话管理按钮 -->
        <div class="chat-session-btns">
          <!-- 新建会话按钮 -->
          <el-button @click="handleSessionCommand('new')" text circle class="session-btn">
            <el-icon :size="20"><Plus /></el-icon>
          </el-button>
          
          <!-- 历史记录按钮 -->
          <el-dropdown trigger="click" @command="handleSessionCommand" v-if="sessionList.length > 0">
            <el-button text circle class="session-btn">
              <el-icon :size="20"><Clock /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <span style="color: #9ca3af; font-size: 12px;">历史会话</span>
                </el-dropdown-item>
                <el-dropdown-item 
                  v-for="session in sessionList" 
                  :key="session.sessionId"
                  :command="`switch:${session.sessionId}`"
                  :class="{ 'is-active': session.sessionId === currentSessionId }"
                >
                  <div class="session-item">
                    <div class="session-info">
                      <div class="session-preview">
                        {{ session.lastMessage?.content || '新对话' }}
                      </div>
                      <div class="session-meta">
                        <span class="session-time">{{ formatSessionTime(session.updateTime) }}</span>
                        <span class="session-count">{{ session.messageCount }}条消息</span>
                      </div>
                    </div>
                    <el-icon v-if="session.sessionId === currentSessionId" class="session-check">
                      <Check />
                    </el-icon>
                  </div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        
        <div class="chat-messages">

          <!-- 聊天消息列表 -->
          <div 
            v-for="(message, index) in validatedMessages" 
            :key="message.id"
            class="message-item"
            :class="[message.type, { 'streaming': message.streaming, 'completed': message.completed }]"
          >
            <div class="message-avatar">
              <UserAvatar 
                v-if="message.type === 'user'" 
                :src="userStore.userAvatar"
                :username="userStore.userName"
                size="medium"
              />
              <div v-else class="ai-avatar">
                <el-icon size="24"><ChatDotSquare /></el-icon>
              </div>
            </div>
            <div class="message-content">
              <div class="message-bubble" :class="{ 'error-message': message.isError }">
                <!-- 流式状态显示 -->
                <div v-if="message.status" class="message-status">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>{{ message.status }}</span>
                </div>
                <!-- 多专家执行过程展示 -->
                <div v-if="message.expertPlan && message.expertPlan.experts.length > 0" class="expert-plan-container">
                  <div class="expert-plan-header" @click="toggleExpertPlan(message)">
                    <span class="plan-icon">🧠</span>
                    <span class="plan-title">多专家协作过程</span>
                    <span class="plan-summary">{{ message.expertPlan.experts.length }}个专家</span>
                    <div class="expand-icon" :class="{ 'rotated': message._expertPlanExpanded }">
                      <el-icon><ArrowRight /></el-icon>
                    </div>
                  </div>
                  
                  <div v-if="message._expertPlanExpanded" class="expert-plan-content">
                    <!-- 执行计划推理 -->
                    <div v-if="message.expertPlan.reasoning || message.expertPlan.plan?.reasoning" class="plan-reasoning">
                      <div class="reasoning-label">📋 执行计划</div>
                      <div class="reasoning-text">{{ message.expertPlan.reasoning || message.expertPlan.plan.reasoning }}</div>
                    </div>
                    
                    <!-- 专家执行列表 -->
                    <div class="experts-timeline">
                      <div 
                        v-for="(expert, index) in message.expertPlan.experts" 
                        :key="index"
                        class="expert-item"
                        :class="expert.status"
                      >
                        <div class="expert-indicator">
                          <div class="expert-number">{{ index + 1 }}</div>
                          <div class="expert-line" v-if="index < message.expertPlan.experts.length - 1"></div>
                        </div>
                        
                        <div class="expert-content">
                          <!-- 精简的专家头部 -->
                          <div class="expert-header-compact">
                            <div class="expert-main-info">
                              <span class="expert-icon">{{ getExpertIcon(expert.name) }}</span>
                              <span class="expert-name">{{ getExpertDisplayName(expert.name) }}</span>
                              <span class="expert-status-badge" :class="expert.status">
                                {{ getExpertStatusText(expert.status) }}
                              </span>
                            </div>
                            <div v-if="expert.status === 'completed'" class="expert-actions">
                              <el-button 
                                text 
                                size="small" 
                                @click="expert._detailsExpanded = !expert._detailsExpanded"
                              >
                                {{ expert._detailsExpanded ? '收起详情' : '查看详情' }}
                                <el-icon><ArrowRight :class="{ 'rotated': expert._detailsExpanded }" /></el-icon>
                              </el-button>
                            </div>
                          </div>
                          
                          <!-- 快速预览：完成汇报（简短版） -->
                          <div v-if="expert.completionReport && expert.status === 'completed' && !expert._detailsExpanded" class="expert-summary">
                            <span class="summary-icon">✅</span>
                            <span class="summary-text">{{ expert.completionReport.substring(0, 60) }}{{ expert.completionReport.length > 60 ? '...' : '' }}</span>
                          </div>
                          
                          <!-- 详细内容（点击"查看详情"后展开） -->
                          <div v-if="expert._detailsExpanded && expert.status === 'completed'" class="expert-details-panel">
                            <!-- 任务描述 -->
                            <div v-if="expert.taskDescription || expert.assignedTask" class="detail-section">
                              <div class="detail-label">📝 任务</div>
                              <div class="detail-content">{{ expert.taskDescription || expert.assignedTask }}</div>
                            </div>
                            
                            <!-- 完整汇报 -->
                            <div v-if="expert.completionReport" class="detail-section">
                              <div class="detail-label">✅ 完成汇报</div>
                              <div class="detail-content">{{ expert.completionReport }}</div>
                            </div>

                            
                            <!-- ReAct 思考过程（简化展示） -->
                            <div v-if="expert.reactInfo" class="detail-section react-section">
                              <div class="detail-label-with-action" @click="expert._reactExpanded = !expert._reactExpanded">
                                <div class="label-with-tags">
                                  <el-icon><DataLine /></el-icon>
                                  <span>ReAct 思考过程</span>
                                  <el-tag size="small" type="info">{{ expert.reactInfo.iterations }}次迭代</el-tag>
                                  <el-tag size="small" :type="expert.reactInfo.goalAchieved ? 'success' : 'warning'">
                                    {{ expert.reactInfo.goalAchieved ? '✓ 达成' : '部分完成' }}
                                  </el-tag>
                                </div>
                                <el-icon class="expand-icon" :class="{ 'rotated': expert._reactExpanded }">
                                  <ArrowRight />
                                </el-icon>
                              </div>
                              
                              <!-- 简化的步骤列表 -->
                              <div v-if="expert._reactExpanded" class="react-steps-simple">
                                <div 
                                  v-for="(step, stepIndex) in expert.reactInfo.steps" 
                                  :key="stepIndex" 
                                  class="step-item"
                                >
                                  <div class="step-info">
                                    <span class="step-num">{{ step.stepNum }}</span>
                                    <span class="step-thought-text">{{ step.thought }}</span>
                                    <span v-if="step.action" class="step-action-badge">{{ step.action.name }}</span>
                                  </div>
                                </div>
                              </div>
                            </div>
                          
                            <!-- MCP工具调用简化展示 -->
                            <div v-if="expert.result?.mcp_calls && expert.result.mcp_calls.length > 0" class="detail-section">
                              <div class="detail-label">
                                <el-icon><Operation /></el-icon>
                                <span>工具调用记录</span>
                                <el-tag size="small" type="info">{{ expert.result.mcp_calls.length }}次</el-tag>
                              </div>
                              <div class="mcp-calls-interactive-list">
                                <div v-for="(mcpCall, mcpIndex) in expert.result.mcp_calls" :key="mcpIndex" class="mcp-call-interactive-item">
                                  <!-- 工具调用头部 - 可点击展开 -->
                                  <div class="mcp-call-header" @click="toggleMcpCallExpand(mcpCall)">
                                    <div class="mcp-call-icon">
                                      <el-icon><Tools /></el-icon>
                                    </div>
                                    <div class="mcp-call-info">
                                      <div class="mcp-call-name">{{ getToolDisplayName(mcpCall.tool) }}</div>
                                      <div class="mcp-call-summary">
                                        {{ getMcpCallSummary(mcpCall) }}
                                      </div>
                                    </div>
                                    <div class="expand-arrow" :class="{ 'rotated': mcpCall._expanded }">
                                      <el-icon><ArrowDown /></el-icon>
                                    </div>
                                  </div>
                                  
                                  <!-- 工具调用详情 - 展开时显示 -->
                                  <div v-if="mcpCall._expanded" class="mcp-call-details">
                                    <!-- 输入参数 -->
                                    <div v-if="mcpCall.input" class="mcp-section">
                                      <div class="mcp-section-title">
                                        <el-icon><Download /></el-icon>
                                        <span>输入参数</span>
                                      </div>
                                      <div class="mcp-params-list">
                                        <div v-for="(param, pIdx) in formatMcpInput(mcpCall.tool, mcpCall.input)" :key="pIdx" class="mcp-param-item">
                                          <span class="param-label">{{ param.label }}:</span>
                                          <span class="param-value">{{ param.value }}</span>
                                        </div>
                                      </div>
                                    </div>
                                    
                                    <!-- 返回结果 -->
                                    <div v-if="mcpCall.output" class="mcp-section">
                                      <div class="mcp-section-title">
                                        <el-icon><Upload /></el-icon>
                                        <span>返回结果</span>
                                        <el-button 
                                          size="small" 
                                          text 
                                          @click.stop="toggleMcpRawView(mcpCall)"
                                          style="margin-left: auto;"
                                        >
                                          {{ mcpCall._showRaw ? '格式化视图' : '原始数据' }}
                                        </el-button>
                                      </div>
                                      
                                      <!-- 格式化视图 -->
                                      <div v-if="!mcpCall._showRaw" class="mcp-output-formatted">
                                        {{ formatMcpOutputDisplay(mcpCall.tool, mcpCall.output) }}
                                      </div>
                                      
                                      <!-- 原始数据视图 -->
                                      <div v-else class="mcp-output-raw">
                                        <pre>{{ JSON.stringify(mcpCall.output, null, 2) }}</pre>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                          
                          <!-- 专家结果详细展示（移到外面，不在详情面板内） -->
                          <div v-if="expert.result && expert.status === 'completed' && !expert._detailsExpanded" class="expert-result-section">
                            <!-- 数据记录专家的结果 -->
                            <div v-if="expert.name === 'DataRecordExpert' && expert.result.has_new_data" class="data-record-result">
                              <div class="record-header">
                                <el-icon><Promotion /></el-icon>
                                <span>已记录健康数据</span>
                              </div>
                              <div class="record-list" v-if="expert.result.records_added && expert.result.records_added.length > 0">
                                <div v-for="(record, rIndex) in expert.result.records_added" :key="rIndex" class="record-item">
                                  <span class="record-type">{{ getDataTypeDisplayName(record.type) }}:</span>
                                  <span class="record-value">{{ formatHealthData(record.data) }}</span>
                                </div>
                              </div>
                            </div>
                            
                            <!-- 问诊专家的评估结果 -->
                            <div v-if="(expert.name === 'ConsultationExpert' || expert.name === '问诊专家') && expert.result.questions && expert.result.questions.length > 0" class="consultation-assessment">
                              <div class="assessment-header">
                                <el-icon><Warning /></el-icon>
                                <span>信息不足，需要更多信息</span>
                              </div>
                              <div class="assessment-questions">
                                <div class="questions-label">需要了解：</div>
                                <ul class="questions-list">
                                  <li v-for="(question, qIndex) in expert.result.questions" :key="qIndex">
                                    {{ question }}
                                  </li>
                                </ul>
                                <div v-if="expert.result.reason" class="assessment-reason">
                                  💡 {{ expert.result.reason }}
                                </div>
                              </div>
                            </div>
                            
                            <!-- 旧版MCP工具调用展示（兼容） -->
                            <div v-else-if="expert.result.mcp_tool" class="expert-mcp-tool">
                              <div class="mcp-tool-header">
                                <el-icon><Operation /></el-icon>
                                <span>MCP工具调用：{{ getToolDisplayName(expert.result.mcp_tool) }}</span>
                              </div>
                              <div class="mcp-tool-data" v-if="expert.result.data || expert.result.knowledge || expert.result.doctors">
                                <div v-if="expert.result.data" class="mcp-data-item">
                                  <span class="data-label">返回数据:</span>
                                  <span class="data-value">{{ getMcpDataSummary(expert.result.data) }}</span>
                                </div>
                                <div v-if="expert.result.knowledge" class="mcp-data-item">
                                  <span class="data-label">知识条目:</span>
                                  <span class="data-value">{{ expert.result.knowledge.length || 0 }}条</span>
                                </div>
                                <div v-if="expert.result.doctors" class="mcp-data-item">
                                  <span class="data-label">医生数量:</span>
                                  <span class="data-value">{{ expert.result.doctors.length || 0 }}位</span>
                                </div>
                              </div>
                            </div>
                            
                            <!-- 专家分析内容（支持展开/折叠和Markdown） -->
                            <div class="expert-analysis-content">
                              <div class="analysis-toggle" @click="toggleExpertAnalysis(expert)">
                                <span class="toggle-label">
                                  {{ expert._analysisExpanded ? '收起' : '查看' }}专家分析
                                </span>
                                <div class="expand-icon" :class="{ 'rotated': expert._analysisExpanded }">
                                  <el-icon><ArrowRight /></el-icon>
                                </div>
                              </div>
                              
                              <div v-if="expert._analysisExpanded" class="analysis-detail">
                                <div class="analysis-markdown" v-html="formatMessage(expert.result.content)"></div>
                              </div>
                              
                              <!-- 不展开时显示预览 -->
                              <div v-else class="analysis-preview">
                                {{ getExpertResultPreview(expert.result) }}
                              </div>
                            </div>
                          </div>
                          
                          <!-- 专家错误信息 -->
                          <div v-if="expert.error" class="expert-error">
                            <el-icon><WarningFilled /></el-icon>
                            <span>{{ expert.error }}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- AI思考过程展示 (豆包风格) -->
                <div v-if="shouldShowThinking(message)" class="thinking-content">
                  <div class="thinking-toggle" @click="toggleThinking(message)" :class="{ 'clickable': true }">
                    <span class="thinking-dots">•••</span>
                    <span class="thinking-label">{{ getThinkingLabel(message) }}</span>
                    <span v-if="message.thinking && message.thinking.trim() && !message._thinkingExpanded" class="thinking-preview">{{ getThinkingPreview(message.thinking) }}</span>
                  </div>
                  <div v-if="message._thinkingExpanded" class="thinking-details">
                    <div v-if="message.thinking && message.thinking.trim()">
                      {{ message.thinking }}
                    </div>
                    <div v-else class="thinking-empty">
                      <span style="color: #c0c4cc; font-style: italic;">思考过程暂无内容</span>
                    </div>
                  </div>
                </div>
                
                <div class="message-text" v-html="formatMessage(message.content)"></div>
                
                <!-- 工具调用结果展示 -->
                <div v-if="message.toolCalls && message.toolCalls.length > 0" class="tool-calls-info">
                  <div class="tool-calls-header">
                    <el-icon><ChatDotSquare /></el-icon>
                    <span>数据查询结果</span>
                  </div>
                  <div 
                    v-for="(toolCall, tcIndex) in message.toolCalls" 
                    :key="tcIndex"
                    class="tool-call-item"
                  >
                    <div class="tool-name">{{ getToolDisplayName(toolCall.tool_name) }}</div>
                    <!-- 统一的工具调用结果交互式组件 -->
                    <div class="tool-result">
                      <div v-if="toolCall.result" class="has-result">
                        <!-- 健康记录添加组件 -->
                        <div v-if="toolCall.tool_name === 'add_health_record'" class="interactive-tool-result add-health-record-result">
                          <div class="result-header" @click="toggleToolDetails(toolCall)">
                            <div class="result-icon">
                              <span>{{ getAddRecordIcon(toolCall) }}</span>
                    </div>
                            <div class="result-info">
                              <div class="result-title">{{ getAddRecordTitle(toolCall) }}</div>
                              <div class="result-summary">{{ getAddRecordValue(toolCall) }}</div>
                            </div>
                            <div class="expand-icon" :class="{ rotated: toolCall._expanded }">
                              <el-icon><ArrowRight /></el-icon>
                  </div>
                </div>
                
                          <div v-if="toolCall._expanded" class="result-details">
                            <div v-for="detail in getAddRecordDetails(toolCall)" :key="detail.label" class="detail-item">
                              <span class="label">{{ detail.label }}:</span>
                              <span v-if="detail.type !== 'json'" class="value">{{ detail.value }}</span>
                              <div v-else class="record-details">
                                <pre>{{ detail.value }}</pre>
                              </div>
                            </div>
                          </div>
                        </div>
                        
                        <!-- 通用组件fallback -->
                        <div v-else class="interactive-tool-result generic-result">
                          <div class="result-header" @click="toggleToolDetails(toolCall)">
                            <div class="result-icon">🔧</div>
                            <div class="result-info">
                              <div class="result-title">{{ getToolDisplayName(toolCall.tool_name) }}</div>
                              <div class="result-summary">点击查看详情</div>
                            </div>
                            <div class="expand-icon" :class="{ rotated: toolCall._expanded }">
                              <el-icon><ArrowRight /></el-icon>
                            </div>
                          </div>
                          
                          <div v-if="toolCall._expanded" class="result-details">
                            <h4>工具信息:</h4>
                            <p><strong>工具名称:</strong> {{ toolCall.tool_name }}</p>
                            <h4>解析结果:</h4>
                            <pre>{{ JSON.stringify(parseToolResult(toolCall), null, 2) }}</pre>
                          </div>
                        </div>
                      </div>
                      <div v-else class="no-result">
                        <small>⚠️ 工具调用无结果</small>
                      </div>
                    </div>
                  </div>
                </div>
                

                
                <!-- Token使用情况显示 -->
                <div v-if="message.usage && userStore.userInfo?.userType === 2" class="usage-info">
                  <small>
                    Token使用: {{ message.usage.prompt_tokens || 0 }} + {{ message.usage.completion_tokens || 0 }} = {{ message.usage.total_tokens || 0 }}
                  </small>
                </div>
                
                <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              </div>
            </div>
          </div>

          <!-- 加载状态 -->
          <div v-if="isLoading" class="message-item ai">
            <div class="ai-avatar">
              <el-icon size="24"><ChatDotSquare /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域（仅对话状态显示） -->
      <div v-if="messages.length > 0" class="chat-input-area">
        <div class="input-container">
          <div class="input-wrapper">
            <el-input
              v-model="currentMessage"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 8 }"
              placeholder="请描述您的症状或健康问题..."
              maxlength="500"
              :show-word-limit="false"
              :disabled="isLoading"
              @keydown.ctrl.enter="sendMessage"
              class="message-input"
            />
            
            <transition name="fade-scale">
              <div class="input-send-btn" v-if="currentMessage.trim() || isLoading">
                <el-button 
                  type="primary" 
                  :loading="isLoading"
                  @click="sendMessage"
                  circle
                  :icon="isLoading ? undefined : ChatDotRound"
                />
              </div>
            </transition>
          </div>
          
          <!-- 底部操作栏 -->
          <div class="input-footer">
            <div class="char-count">{{ currentMessage.length }}/500</div>
            <div class="footer-actions">
              <el-button @click="clearChat" :disabled="isLoading" size="small" text>
                清空对话
              </el-button>
              <el-button @click="refreshHistory" :disabled="isLoading" size="small" text>
                同步历史
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, computed, defineComponent } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import {
  ChatDotSquare,
  User,
  ChatDotRound,
  ArrowRight,
  Loading,
  Operation,
  WarningFilled,
  Warning,
  Promotion,
  Document,
  Paperclip,
  MagicStick,
  ChatLineSquare,
  TrendCharts,
  Food,
  More,
  ArrowDown,
  Plus,
  Check,
  Refresh,
  Clock
} from '@element-plus/icons-vue'
import { agentAPI } from '@/api/agent'
import { useUserStore } from '@/stores/user'
import MarkdownIt from 'markdown-it'
import UserAvatar from '@/components/common/UserAvatar.vue'

// 用户store
const userStore = useUserStore()

// 计算属性：验证和过滤消息，防止重复AI消息
// 个性化问候语
const greetingMessage = computed(() => {
  const hour = new Date().getHours()
  const userName = userStore.userName || '朋友'
  
  let greeting = ''
  if (hour >= 5 && hour < 12) {
    greeting = '早上好'
  } else if (hour >= 12 && hour < 14) {
    greeting = '中午好'
  } else if (hour >= 14 && hour < 18) {
    greeting = '下午好'
  } else if (hour >= 18 && hour < 22) {
    greeting = '晚上好'
  } else {
    greeting = '夜深了'
  }
  
  return `${greeting}，${userName}`
})

const validatedMessages = computed(() => {
  const result = []
  const seenAiIds = new Set()
  
  for (const message of messages.value) {
    if (message.type === 'ai') {
      // 检查AI消息重复
      if (seenAiIds.has(message.id)) {
        console.warn('发现重复的AI消息ID:', message.id)
        continue
      }
      seenAiIds.add(message.id)
      
      // 检查AI消息状态异常
      if (message.streaming && message.completed) {
        console.warn('AI消息状态异常（同时标记为streaming和completed）:', message.id)
        // 修复状态
        message.streaming = false
      }
    }
    
    result.push(message)
  }
  

  
  return result
})

// =================== 交互式组件定义区域 ===================

// 工具结果数据解析辅助函数
const parseToolResult = (toolCall) => {
  try {
    let result = toolCall.result
    
    // 如果result是字符串，尝试解析
    if (typeof result === 'string') {
      result = JSON.parse(result)
    }
    
    // 处理嵌套的数据结构
    if (result?.data?.content?.[0]?.text) {
      // 从content[0].text中解析JSON
      return JSON.parse(result.data.content[0].text)
    } else if (result?.data?.structuredContent?.result) {
      // 从structuredContent.result中解析JSON
      return JSON.parse(result.data.structuredContent.result)
    } else if (result?.data) {
      // 直接使用data
      return result.data
    } else {
      // 直接使用result
  return result
    }
  } catch (e) {
    console.warn('Failed to parse tool result:', e)
    return toolCall.result
  }
}

// 1. 健康记录查询结果组件
const HealthRecordsResult = defineComponent({
  props: ['toolCall'],
  emits: ['toggle'],
  setup(props, { emit }) {
    const expanded = ref(false)
    
    const toggle = () => {
      expanded.value = !expanded.value
      emit('toggle', props.toolCall)
    }
    
    const getRecordsSummary = () => {
      const data = parseToolResult(props.toolCall)
      if (!data?.health_records) return '无记录'
      
      const types = Object.keys(data.health_records)
      const total = Object.values(data.health_records).reduce((sum, records) => sum + records.length, 0)
      return `${total}条记录 (${types.map(t => getRecordTypeName(t)).join('、')})`
    }
    
    return { expanded, toggle, getRecordsSummary, getRecordTypeName, parseToolResult }
  },
  template: `
    <div class="interactive-tool-result health-records-result">
      <div class="result-header" @click="toggle">
        <div class="result-icon">📊</div>
        <div class="result-info">
          <div class="result-title">健康记录查询完成</div>
          <div class="result-summary">{{ getRecordsSummary() }}</div>
        </div>
        <div class="expand-icon" :class="{ rotated: expanded }">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      
      <div v-if="expanded" class="result-details">
        <div v-if="parseToolResult(toolCall)?.health_records" class="records-grid">
          <div v-for="(records, type) in parseToolResult(toolCall).health_records" :key="type" class="record-type-card">
            <h4>{{ getRecordTypeName(type) }}</h4>
            <div class="record-stats">
              <span class="count">{{ records.length }}条</span>
              <span v-if="records.length > 0" class="latest">
                最新: {{ new Date(records[0].measureTime).toLocaleDateString() }}
              </span>
            </div>
            <div v-if="records.length > 0" class="latest-value">
              <span v-if="type === 'glucose'">{{ records[0].value }} mmol/L</span>
              <span v-else-if="type === 'pressure'">{{ records[0].systolic }}/{{ records[0].diastolic }} mmHg</span>
              <span v-else-if="type === 'weight'">{{ records[0].value || records[0].weight }} kg</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})

// 2. 医生列表查询结果组件
const DoctorListResult = defineComponent({
  props: ['toolCall'],
  emits: ['toggle', 'action'],
  setup(props, { emit }) {
    const expanded = ref(false)
    
    const toggle = () => {
      expanded.value = !expanded.value
      emit('toggle', props.toolCall)
    }
    
    const getDoctorsSummary = () => {
      const data = parseToolResult(props.toolCall)
      const doctors = data?.doctors || []
      return `找到 ${doctors.length} 位医生`
    }
    
    const viewDoctorDetail = (doctor) => {
      emit('action', 'viewDoctorDetail', { doctor })
    }
    
    return { expanded, toggle, getDoctorsSummary, viewDoctorDetail, parseToolResult }
  },
  template: `
    <div class="interactive-tool-result doctor-list-result">
      <div class="result-header" @click="toggle">
        <div class="result-icon">👨‍⚕️</div>
        <div class="result-info">
          <div class="result-title">医生查询完成</div>
          <div class="result-summary">{{ getDoctorsSummary() }}</div>
        </div>
        <div class="expand-icon" :class="{ rotated: expanded }">
          <el-icon><arrow-right /></el-icon>
        </div>
      </div>
      
      <div v-if="expanded" class="result-details">
        <div v-if="parseToolResult(toolCall)?.doctors?.length" class="doctors-grid">
          <div v-for="doctor in parseToolResult(toolCall).doctors" :key="doctor.id" class="doctor-card">
            <div class="doctor-info">
              <h4>{{ doctor.real_name || doctor.name }}</h4>
              <p class="department">{{ doctor.department }}</p>
              <p class="title">{{ doctor.title }}</p>
            </div>
            <div class="doctor-status">
              <span :class="['status', doctor.online_status ? 'online' : 'offline']">
                {{ doctor.online_status ? '在线' : '离线' }}
              </span>
            </div>
          </div>
        </div>
        <div v-else class="no-data">暂无医生信息</div>
      </div>
    </div>
  `
})

// 3. 用户咨询记录结果组件
const ConsultationsResult = defineComponent({
  props: ['toolCall'],
  emits: ['toggle', 'action'],
  setup(props, { emit }) {
    const expanded = ref(false)
    
    const toggle = () => {
      expanded.value = !expanded.value
      emit('toggle', props.toolCall)
    }
    
    const getConsultationsSummary = () => {
      const data = parseToolResult(props.toolCall)
      const consultations = data?.consultations || []
      return `找到 ${consultations.length} 条咨询记录`
    }
    
    return { expanded, toggle, getConsultationsSummary, parseToolResult }
  },
  template: `
    <div class="interactive-tool-result consultations-result">
      <div class="result-header" @click="toggle">
        <div class="result-icon">💬</div>
        <div class="result-info">
          <div class="result-title">咨询记录查询完成</div>
          <div class="result-summary">{{ getConsultationsSummary() }}</div>
        </div>
        <div class="expand-icon" :class="{ rotated: expanded }">
          <el-icon><arrow-right /></el-icon>
        </div>
      </div>
      
      <div v-if="expanded" class="result-details">
        <div v-if="parseToolResult(toolCall)?.consultations?.length" class="consultations-list">
          <div v-for="consultation in parseToolResult(toolCall).consultations" :key="consultation._id" class="consultation-item">
            <div class="consultation-header">
              <span class="consultation-title">{{ consultation.consultationNo || '咨询记录' }}</span>
              <span class="consultation-date">{{ new Date(consultation.createTime).toLocaleDateString() }}</span>
            </div>
            <div class="consultation-status">
              <span :class="['status', consultation.status]">
                {{ consultation.status === 1 ? '待处理' : 
                   consultation.status === 2 ? '进行中' : 
                   consultation.status === 3 ? '已完成' : '已取消' }}
              </span>
            </div>
          </div>
        </div>
        <div v-else class="no-data">暂无咨询记录</div>
      </div>
    </div>
  `
})

// 4. 科室信息结果组件
const DepartmentInfoResult = defineComponent({
  props: ['toolCall'],
  emits: ['toggle'],
  setup(props, { emit }) {
    const expanded = ref(false)
    
    const toggle = () => {
      expanded.value = !expanded.value
      emit('toggle', props.toolCall)
    }
    
    const getDepartmentsSummary = () => {
      const data = parseToolResult(props.toolCall)
      if (data?.departments) {
        return `找到 ${data.departments.length} 个科室`
      } else if (data?.department) {
        return `${data.department} 科室信息`
      }
      return '科室信息'
    }
    
    return { expanded, toggle, getDepartmentsSummary, parseToolResult }
  },
  template: `
    <div class="interactive-tool-result department-info-result">
      <div class="result-header" @click="toggle">
        <div class="result-icon">🏥</div>
        <div class="result-info">
          <div class="result-title">科室信息查询完成</div>
          <div class="result-summary">{{ getDepartmentsSummary() }}</div>
        </div>
        <div class="expand-icon" :class="{ rotated: expanded }">
          <el-icon><arrow-right /></el-icon>
        </div>
      </div>
      
      <div v-if="expanded" class="result-details">
        <div v-if="parseToolResult(toolCall)?.departments?.length" class="departments-grid">
          <div v-for="dept in parseToolResult(toolCall).departments" :key="dept.department" class="department-card">
            <h4>{{ dept.department }}</h4>
            <div class="department-stats">
              <span>医生数量: {{ dept.doctor_count || 0 }}</span>
              <span>在线: {{ dept.online_count || 0 }}</span>
              <span>咨询数: {{ dept.total_consultations || 0 }}</span>
            </div>
          </div>
        </div>
        <div v-else-if="parseToolResult(toolCall)?.doctors" class="doctors-grid">
          <div v-for="doctor in parseToolResult(toolCall).doctors" :key="doctor.id" class="doctor-card">
            <div class="doctor-info">
              <h4>{{ doctor.real_name }}</h4>
              <p class="title">{{ doctor.title }}</p>
            </div>
            <div class="doctor-status">
              <span :class="['status', doctor.online_status ? 'online' : 'offline']">
                {{ doctor.online_status ? '在线' : '离线' }}
              </span>
            </div>
          </div>
        </div>
        <div v-else class="no-data">暂无科室信息</div>
      </div>
    </div>
  `
})

// 5. 系统概览结果组件  
const SystemOverviewResult = defineComponent({
  props: ['toolCall'],
  emits: ['toggle'],
  setup(props, { emit }) {
    const expanded = ref(false)
    
    const toggle = () => {
      expanded.value = !expanded.value
      emit('toggle', props.toolCall)
    }
    
    const getOverviewSummary = () => {
      const data = parseToolResult(props.toolCall)
      return `系统概览 (近${data?.overview_period_days || 7}天)`
    }
    
    return { expanded, toggle, getOverviewSummary, parseToolResult }
  },
  template: `
    <div class="interactive-tool-result system-overview-result">
      <div class="result-header" @click="toggle">
        <div class="result-icon">📈</div>
        <div class="result-info">
          <div class="result-title">系统概览查询完成</div>
          <div class="result-summary">{{ getOverviewSummary() }}</div>
        </div>
        <div class="expand-icon" :class="{ rotated: expanded }">
          <el-icon><arrow-right /></el-icon>
        </div>
      </div>
      
      <div v-if="expanded" class="result-details">
        <div v-if="parseToolResult(toolCall)" class="overview-grid">
          <div class="overview-item">
            <h4>用户统计</h4>
            <p>总用户: {{ parseToolResult(toolCall).users?.total_users || 0 }}</p>
            <p>活跃用户: {{ parseToolResult(toolCall).users?.active_users || 0 }}</p>
          </div>
          <div class="overview-item">
            <h4>咨询统计</h4>
            <p>总咨询: {{ parseToolResult(toolCall).consultations?.total || 0 }}</p>
            <p>最近咨询: {{ parseToolResult(toolCall).consultations?.recent || 0 }}</p>
          </div>
          <div class="overview-item">
            <h4>医生统计</h4>
            <p>在线医生: {{ parseToolResult(toolCall).doctors?.online_doctors || 0 }}</p>
            <p>总医生: {{ parseToolResult(toolCall).doctors?.total_doctors || 0 }}</p>
          </div>
        </div>
      </div>
    </div>
  `
})

// 6. 医生推荐结果组件
const DoctorSearchResult = defineComponent({
  props: ['toolCall'],
  emits: ['toggle', 'action'],
  setup(props, { emit }) {
    const expanded = ref(false)
    
    const toggle = () => {
      expanded.value = !expanded.value
      emit('toggle', props.toolCall)
    }
    
    const getSearchSummary = () => {
      const data = parseToolResult(props.toolCall)
      const doctors = data?.recommended_doctors || []
      const dept = data?.recommended_department || ''
      return `推荐 ${doctors.length} 位${dept ? dept : ''}医生`
    }
    
    return { expanded, toggle, getSearchSummary, parseToolResult }
  },
  template: `
    <div class="interactive-tool-result doctor-search-result">
      <div class="result-header" @click="toggle">
        <div class="result-icon">🔍</div>
        <div class="result-info">
          <div class="result-title">医生推荐完成</div>
          <div class="result-summary">{{ getSearchSummary() }}</div>
        </div>
        <div class="expand-icon" :class="{ rotated: expanded }">
          <el-icon><arrow-right /></el-icon>
        </div>
      </div>
      
      <div v-if="expanded" class="result-details">
        <div v-if="parseToolResult(toolCall)?.recommended_department" class="recommendation-info">
          <p><strong>推荐科室:</strong> {{ parseToolResult(toolCall).recommended_department }}</p>
          <p><strong>匹配度:</strong> {{ Math.round((parseToolResult(toolCall).confidence_score || 0) * 100) }}%</p>
        </div>
        <div v-if="parseToolResult(toolCall)?.recommended_doctors?.length" class="doctors-grid">
          <div v-for="doctor in parseToolResult(toolCall).recommended_doctors" :key="doctor.id" class="doctor-card recommended">
            <div class="doctor-info">
              <h4>{{ doctor.real_name }}</h4>
              <p class="department">{{ doctor.department }}</p>
              <p class="title">{{ doctor.title }}</p>
              <p v-if="doctor.speciality" class="specialization">专长: {{ doctor.speciality }}</p>
            </div>
            <div class="doctor-status">
              <span :class="['status', doctor.online_status ? 'online' : 'offline']">
                {{ doctor.online_status ? '在线' : '离线' }}
              </span>
              <div v-if="doctor.availability_score" class="match-score">
                <span class="score">可用性: {{ Math.round(doctor.availability_score * 100) }}%</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="no-data">暂无推荐医生</div>
      </div>
    </div>
  `
})

// 7. 咨询详情结果组件
const ConsultationDetailResult = defineComponent({
  props: ['toolCall'],
  emits: ['toggle'],
  setup(props, { emit }) {
    const expanded = ref(false)
    
    const toggle = () => {
      expanded.value = !expanded.value
      emit('toggle', props.toolCall)
    }
    
    const getDetailSummary = () => {
      const data = parseToolResult(props.toolCall)
      const consultation = data?.consultation
      return consultation ? `咨询详情: ${consultation.consultationNo || '无编号'}` : '咨询详情查询完成'
    }
    
    return { expanded, toggle, getDetailSummary, parseToolResult }
  },
  template: `
    <div class="interactive-tool-result consultation-detail-result">
      <div class="result-header" @click="toggle">
        <div class="result-icon">📋</div>
        <div class="result-info">
          <div class="result-title">咨询详情查询完成</div>
          <div class="result-summary">{{ getDetailSummary() }}</div>
        </div>
        <div class="expand-icon" :class="{ rotated: expanded }">
          <el-icon><arrow-right /></el-icon>
        </div>
      </div>
      
      <div v-if="expanded" class="result-details">
        <div v-if="parseToolResult(toolCall)?.consultation" class="detail-content">
          <div class="detail-header">
            <h4>{{ parseToolResult(toolCall).consultation.consultationNo || '咨询详情' }}</h4>
            <span class="detail-date">{{ new Date(parseToolResult(toolCall).consultation.createTime).toLocaleString() }}</span>
          </div>
          <div class="detail-body">
            <div v-if="parseToolResult(toolCall).patient_info" class="patient-info">
              <p><strong>患者:</strong> {{ parseToolResult(toolCall).patient_info.real_name }}</p>
            </div>
            <div v-if="parseToolResult(toolCall).doctor_info" class="doctor-info">
              <p><strong>医生:</strong> {{ parseToolResult(toolCall).doctor_info.real_name }} ({{ parseToolResult(toolCall).doctor_info.department }})</p>
            </div>
            <div class="detail-status">
              <span :class="['status', parseToolResult(toolCall).consultation.status]">
                {{ parseToolResult(toolCall).consultation.status === 1 ? '待处理' : 
                   parseToolResult(toolCall).consultation.status === 2 ? '进行中' : 
                   parseToolResult(toolCall).consultation.status === 3 ? '已完成' : '已取消' }}
              </span>
            </div>
            <div v-if="parseToolResult(toolCall).message_count" class="message-count">
              <p>消息数量: {{ parseToolResult(toolCall).message_count }}</p>
            </div>
          </div>
        </div>
        <div v-else class="no-data">无详细信息</div>
      </div>
    </div>
  `
})

// 8. 添加健康记录结果组件 (保持原有的复杂交互)
const AddHealthRecordResult = defineComponent({
  props: ['toolCall'],
  emits: ['toggle'],
  setup(props, { emit }) {
    const expanded = ref(false)
    
    const toggle = () => {
      expanded.value = !expanded.value
      emit('toggle', props.toolCall)
    }
    
    return { 
      expanded, 
      toggle, 
      getAddRecordIcon, 
      getAddRecordTitle, 
      getAddRecordValue, 
      getAddRecordDetails 
    }
  },
  template: `
    <div class="interactive-tool-result add-health-record-result">
      <div class="result-header" @click="toggle">
        <div class="result-icon">
          <span>{{ getAddRecordIcon(toolCall) }}</span>
        </div>
        <div class="result-info">
          <div class="result-title">{{ getAddRecordTitle(toolCall) }}</div>
          <div class="result-summary">{{ getAddRecordValue(toolCall) }}</div>
        </div>
        <div class="expand-icon" :class="{ rotated: expanded }">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      
      <div v-if="expanded" class="result-details">
        <div v-for="detail in getAddRecordDetails(toolCall)" :key="detail.label" class="detail-item">
          <span class="label">{{ detail.label }}:</span>
          <span v-if="detail.type !== 'json'" class="value">{{ detail.value }}</span>
          <div v-else class="record-details">
            <pre>{{ detail.value }}</pre>
          </div>
        </div>
      </div>
    </div>
  `
})

// 9. 通用工具结果组件 (fallback)
const GenericToolResult = defineComponent({
  props: ['toolCall'],
  emits: ['toggle'],
  setup(props, { emit }) {
    const expanded = ref(false)
    
    const toggle = () => {
      expanded.value = !expanded.value
      emit('toggle', props.toolCall)
  
    }
    
    return { expanded, toggle }
  },
  template: `
    <div class="interactive-tool-result generic-result" style="border: 2px solid #409EFF; margin: 8px 0;">
      <div class="result-header" @click="toggle" style="cursor: pointer; padding: 12px; background: #f0f9ff;">
        <div class="result-icon">🔧</div>
        <div class="result-info">
          <div class="result-title">{{ toolCall.tool_name || '工具调用' }}</div>
          <div class="result-summary">点击查看详情 ({{ expanded ? '已展开' : '已折叠' }})</div>
        </div>
                 <div class="expand-icon" :class="{ rotated: expanded }">
           <el-icon><ArrowRight /></el-icon>
         </div>
       </div>
       
       <div v-if="expanded" class="result-details" style="padding: 12px; background: #fff; border-top: 1px solid #e4e7ed;">
        <h4>工具信息:</h4>
        <p><strong>工具名称:</strong> {{ toolCall.tool_name }}</p>
        <h4>原始结果:</h4>
        <pre style="background: #f8f9fa; padding: 8px; border-radius: 4px; overflow-x: auto;">{{ JSON.stringify(toolCall, null, 2) }}</pre>
      </div>
    </div>
  `
})

// 工具组件映射和管理方法 - 现在返回实际的组件引用
const getToolComponent = (toolName) => {
  const componentMap = {
    'query_user_health_records': HealthRecordsResult,
    'query_doctor_list': DoctorListResult, 
    'query_user_consultations': ConsultationsResult,
    'query_department_info': DepartmentInfoResult,
    'query_system_overview': SystemOverviewResult,
    'search_doctors_by_condition': DoctorSearchResult,
    'query_consultation_detail': ConsultationDetailResult,
    'add_health_record': AddHealthRecordResult
  }
  return componentMap[toolName] || GenericToolResult
}

// 工具详情切换
const toggleToolDetails = (toolCall) => {
  toolCall._expanded = !toolCall._expanded
}

// 思考内容切换
const toggleThinking = (message) => {
  // 初始化展开状态（如果未设置）
  if (message._thinkingExpanded === undefined) {
    message._thinkingExpanded = false
  }
  
  // 切换展开状态（无论是否有thinking内容）
  message._thinkingExpanded = !message._thinkingExpanded
}

// 多专家计划切换
const toggleExpertPlan = (message) => {
  if (message._expertPlanExpanded === undefined) {
    message._expertPlanExpanded = false
  }
  message._expertPlanExpanded = !message._expertPlanExpanded
}

// 获取专家图标
const getExpertIcon = (expertName) => {
  const icons = {
    'DataRecordExpert': '📝',
    'ConsultationExpert': '🩹',
    'DataExpert': '📊',
    'KnowledgeExpert': '📚',
    'DiagnosisExpert': '🩺',
    'DoctorExpert': '👨‍⚕️',
    'SynthesisExpert': '🔄'
  }
  return icons[expertName] || '🤖'
}

// 获取专家显示名称
const getExpertDisplayName = (expertName) => {
  const names = {
    'DataRecordExpert': '数据记录专家',
    'ConsultationExpert': '问诊专家',
    'DataExpert': '数据专家',
    'KnowledgeExpert': '知识专家',
    'DiagnosisExpert': '诊断专家',
    'DoctorExpert': '医生推荐专家',
    'SynthesisExpert': '综合专家'
  }
  return names[expertName] || expertName
}

// 获取专家状态文本
const getExpertStatusText = (status) => {
  const statusMap = {
    'pending': '等待中',
    'running': '执行中',
    'completed': '已完成',
    'error': '执行失败'
  }
  return statusMap[status] || status
}

// 获取专家结果预览
const getExpertResultPreview = (result) => {
  if (!result) return ''
  
  // 提取关键内容预览
  const content = result.content || result.analysis || result.explanation || result.recommendation || ''
  if (content.length > 100) {
    return content.substring(0, 100) + '...'
  }
  return content
}

// 切换专家分析内容展开/折叠
const toggleExpertAnalysis = (expert) => {
  if (expert._analysisExpanded === undefined) {
    expert._analysisExpanded = false
  }
  expert._analysisExpanded = !expert._analysisExpanded
}

// 切换MCP调用详情展开/折叠
const toggleMcpCall = (mcpCall) => {
  if (mcpCall._expanded === undefined) {
    mcpCall._expanded = false
  }
  mcpCall._expanded = !mcpCall._expanded
}

// 新的MCP调用展开方法（与上面的兼容）
const toggleMcpCallExpand = (mcpCall) => {
  if (mcpCall._expanded === undefined) {
    mcpCall._expanded = false
  }
  mcpCall._expanded = !mcpCall._expanded
}

// 切换MCP原始日志显示
const toggleMcpRawView = (mcpCall) => {
  if (mcpCall._showRaw === undefined) {
    mcpCall._showRaw = false
  }
  mcpCall._showRaw = !mcpCall._showRaw
}

// 获取MCP调用摘要
const getMcpCallSummary = (mcpCall) => {
  if (!mcpCall.output) return '执行中...'
  
  // 尝试解析输出
  try {
    let output = mcpCall.output
    if (typeof output === 'string') {
      output = JSON.parse(output)
    }
    
    // 提取实际数据（处理嵌套JSON）
    const actualData = extractMcpData(output)
    
    // 根据不同工具类型返回不同摘要
    switch (mcpCall.tool) {
      case 'query_user_health_records':
        if (actualData.health_records) {
          const records = actualData.health_records
          let total = 0
          if (records.glucose) total += records.glucose.length
          if (records.pressure) total += records.pressure.length
          if (records.weight) total += records.weight.length
          return `查询到 ${total} 条记录`
        }
        return '查询完成'
        
      case 'add_health_record':
        if (output.success) {
          return '记录添加成功'
        }
        return '添加失败'
        
      case 'search_diabetes_knowledge':
        if (actualData.search_results || actualData.knowledge) {
          const results = actualData.search_results || actualData.knowledge || []
          return `找到 ${results.length} 条相关知识`
        }
        return '知识检索完成'
        
      case 'query_doctor_list':
        if (actualData.doctors) {
          return `找到 ${actualData.doctors.length} 位医生`
        }
        return '医生查询完成'
        
      default:
        if (output.success || actualData.success) {
          return '执行成功'
        } else if (actualData.error || output.error) {
          return `执行失败: ${actualData.error || output.error}`
        }
        return '执行完成'
    }
  } catch (e) {
    return '查看详情'
  }
}

// 提取MCP返回的实际数据（处理嵌套JSON）
const extractMcpData = (mcpResponse) => {
  if (!mcpResponse || typeof mcpResponse !== 'object') {
    return mcpResponse
  }
  
  // 第一层：提取data字段
  let data = mcpResponse.data || {}
  
  // 如果data是字典且包含structuredContent或content，说明是嵌套格式
  if (typeof data === 'object' && data !== null) {
    // 尝试从structuredContent.result获取
    if (data.structuredContent && data.structuredContent.result) {
      try {
        const parsed = JSON.parse(data.structuredContent.result)
        console.log('✅ 从structuredContent.result解析JSON成功')
        return parsed
      } catch (e) {
        console.warn('⚠️ structuredContent.result解析失败:', e)
      }
    }
    
    // 尝试从content[0].text获取
    if (data.content && Array.isArray(data.content) && data.content.length > 0) {
      const firstItem = data.content[0]
      if (firstItem.text) {
        try {
          const parsed = JSON.parse(firstItem.text)
          console.log('✅ 从content[0].text解析JSON成功')
          return parsed
        } catch (e) {
          console.warn('⚠️ content[0].text解析失败:', e)
        }
      }
    }
  }
  
  // 如果不是嵌套格式，直接返回data
  return data
}

// 格式化MCP输出显示
const formatMcpOutputDisplay = (tool, output) => {
  if (!output) return '无返回数据'
  
  try {
    let parsedOutput = output
    if (typeof output === 'string') {
      parsedOutput = JSON.parse(output)
    }
    
    // 提取实际数据（处理嵌套JSON）
    const actualData = extractMcpData(parsedOutput)
    
    // 根据不同工具类型返回不同格式
    switch (tool) {
      case 'query_user_health_records':
        if (actualData.health_records) {
          const records = actualData.health_records
          let summary = []
          if (records.glucose && records.glucose.length > 0) {
            summary.push(`血糖记录: ${records.glucose.length}条`)
          }
          if (records.pressure && records.pressure.length > 0) {
            summary.push(`血压记录: ${records.pressure.length}条`)
          }
          if (records.weight && records.weight.length > 0) {
            summary.push(`体重记录: ${records.weight.length}条`)
          }
          return summary.length > 0 ? summary.join(', ') : '无数据'
        }
        return actualData.error || '查询失败'
        
      case 'add_health_record':
        if (parsedOutput.success) {
          const details = parsedOutput.record_details
          if (details) {
            return `成功添加${getRecordTypeName(details.type)}，当前共${details.total_records_count}条记录`
          }
          return '记录添加成功'
        }
        return parsedOutput.error || '添加失败'
        
      case 'search_diabetes_knowledge':
        if (actualData.search_results || actualData.knowledge) {
          const results = actualData.search_results || actualData.knowledge || []
          const avgScore = actualData.avg_score || 0
          return `检索到${results.length}条知识${avgScore > 0 ? `，平均相关度: ${avgScore.toFixed(2)}` : ''}`
        }
        return actualData.error || '检索失败'
        
      case 'query_doctor_list':
        if (actualData.doctors) {
          return `查询到${actualData.doctors.length}位医生`
        }
        return actualData.error || '查询失败'
        
      default:
        if (output.success || actualData.success) {
          return actualData.message || output.message || '执行成功'
        }
        return actualData.error || output.error || '执行失败'
    }
  } catch (e) {
    return '数据格式异常，请查看原始数据'
  }
}

// 格式化MCP输入参数显示
const formatMcpInput = (tool, input) => {
  if (!input) return []
  
  const formatted = []
  
  switch (tool) {
    case 'query_user_health_records':
      formatted.push({ label: '用户ID', value: input.user_id || 'N/A' })
      formatted.push({ label: '查询天数', value: `${input.days || 0} 天` })
      if (input.limit) formatted.push({ label: '限制数量', value: input.limit })
      if (input.record_type) formatted.push({ label: '记录类型', value: input.record_type })
      break
      
    case 'search_diabetes_knowledge':
      formatted.push({ label: '查询内容', value: input.query || 'N/A' })
      formatted.push({ label: '返回数量', value: input.top_k || 5 })
      if (input.similarity_threshold) {
        formatted.push({ label: '相似度阈值', value: input.similarity_threshold })
      }
      if (input.category_filter) {
        formatted.push({ label: '分类过滤', value: input.category_filter })
      }
      break
      
    case 'query_doctor_list':
      if (input.status) formatted.push({ label: '状态', value: input.status === 'online' ? '在线' : '离线' })
      if (input.department) formatted.push({ label: '科室', value: input.department })
      if (input.limit) formatted.push({ label: '限制数量', value: input.limit })
      break
      
    case 'add_health_record':
      formatted.push({ label: '用户ID', value: input.user_id || 'N/A' })
      formatted.push({ label: '记录类型', value: input.record_type || 'N/A' })
      if (input.data) {
        formatted.push({ label: '记录数据', value: JSON.stringify(input.data), isJson: true })
      }
      break
      
    default:
      // 通用格式化
      Object.keys(input).forEach(key => {
        formatted.push({ label: key, value: input[key] })
      })
  }
  
  return formatted
}

// 格式化MCP输出结果显示
const formatMcpOutput = (tool, output) => {
  if (!output) return { type: 'empty', data: [] }
  
  // 提取实际数据
  let actualData = output
  if (output.data) {
    // 尝试解析嵌套的JSON字符串
    if (output.data.structuredContent?.result) {
      try {
        actualData = JSON.parse(output.data.structuredContent.result)
      } catch (e) {
        // 尝试content[0].text
        if (output.data.content?.[0]?.text) {
          try {
            actualData = JSON.parse(output.data.content[0].text)
          } catch (e2) {
            actualData = output.data
          }
        } else {
          actualData = output.data
        }
      }
    } else if (output.data.content?.[0]?.text) {
      try {
        actualData = JSON.parse(output.data.content[0].text)
      } catch (e) {
        actualData = output.data
      }
    } else {
      actualData = output.data
    }
  }
  
  const formatted = []
  
  switch (tool) {
    case 'query_user_health_records':
      if (actualData.user_info) {
        formatted.push({ 
          label: '用户信息', 
          value: `${actualData.user_info.real_name || 'N/A'} (ID: ${actualData.user_info.id})`,
          icon: '👤'
        })
      }
      if (actualData.health_records) {
        const records = actualData.health_records
        let totalCount = 0
        const details = []
        
        if (records.glucose?.length) {
          totalCount += records.glucose.length
          details.push(`血糖 ${records.glucose.length} 条`)
        }
        if (records.pressure?.length) {
          totalCount += records.pressure.length
          details.push(`血压 ${records.pressure.length} 条`)
        }
        if (records.weight?.length) {
          totalCount += records.weight.length
          details.push(`体重 ${records.weight.length} 条`)
        }
        if (records.height?.length) {
          totalCount += records.height.length
          details.push(`身高 ${records.height.length} 条`)
        }
        
        formatted.push({ 
          label: '健康记录', 
          value: `共 ${totalCount} 条记录`,
          details: details.join(', '),
          icon: '📊'
        })
      }
      break
      
    case 'search_diabetes_knowledge':
      if (actualData.search_results) {
        formatted.push({ 
          label: '检索结果', 
          value: `${actualData.search_results.length} 条知识`,
          icon: '📚'
        })
        if (actualData.search_summary) {
          formatted.push({
            label: '检索统计',
            value: `找到 ${actualData.search_summary.total_found || 0} 条，返回 ${actualData.search_summary.returned_count || 0} 条`,
            icon: '📈'
          })
          if (actualData.search_summary.cache_hit !== undefined) {
            formatted.push({
              label: '缓存',
              value: actualData.search_summary.cache_hit ? '命中' : '未命中',
              icon: '💾'
            })
          }
        }
      }
      break
      
    case 'query_doctor_list':
      if (actualData.doctors) {
        formatted.push({ 
          label: '医生列表', 
          value: `${actualData.doctors.length} 位医生`,
          icon: '👨‍⚕️'
        })
        if (actualData.total_count !== undefined) {
          formatted.push({
            label: '总数',
            value: actualData.total_count,
            icon: '🔢'
          })
        }
      }
      break
      
    case 'rag_health_check':
      if (actualData.service_status) {
        formatted.push({ 
          label: '服务状态', 
          value: actualData.service_status === 'healthy' ? '健康' : '异常',
          icon: '🏥'
        })
      }
      if (actualData.total_documents !== undefined) {
        formatted.push({ 
          label: '文档总数', 
          value: actualData.total_documents,
          icon: '📄'
        })
      }
      if (actualData.model_loaded !== undefined) {
        formatted.push({ 
          label: '模型加载', 
          value: actualData.model_loaded ? '已加载' : '未加载',
          icon: '🤖'
        })
      }
      break
      
    case 'add_health_record':
      if (actualData.success) {
        formatted.push({ 
          label: '添加状态', 
          value: '成功',
          icon: '✅'
        })
        if (actualData.record_id) {
          formatted.push({ 
            label: '记录ID', 
            value: actualData.record_id,
            icon: '🆔'
          })
        }
      } else {
        formatted.push({ 
          label: '添加状态', 
          value: '失败',
          icon: '❌'
        })
      }
      break
      
    default:
      // 通用格式化
      if (actualData.success !== undefined) {
        formatted.push({ 
          label: '执行状态', 
          value: actualData.success ? '成功' : '失败',
          icon: actualData.success ? '✅' : '❌'
        })
      }
  }
  
  return { type: 'formatted', data: formatted, rawData: actualData }
}

// 获取MCP数据摘要
const getMcpDataSummary = (data) => {
  if (!data) return '无'
  
  try {
    // 如果是健康记录数据
    if (data.records) {
      const count = data.total_count || data.records.length || 0
      return `${count}条记录`
    }
    
    // 如果是对象，统计字段数
    if (typeof data === 'object') {
      const keys = Object.keys(data)
      return `${keys.length}个字段`
    }
    
    return '已返回'
  } catch (e) {
    return '已返回'
  }
}

// 获取数据类型显示名称
const getDataTypeDisplayName = (type) => {
  const names = {
    'glucose': '血糖',
    'pressure': '血压',
    'weight': '体重',
    'height': '身高',
    'bmi': 'BMI'
  }
  return names[type] || type
}

// 格式化健康数据
const formatHealthData = (data) => {
  if (!data) return ''
  
  try {
    // 血糖数据
    if (data.value && data.unit === 'mmol/L') {
      return `${data.value} ${data.unit} ${data.measureType ? `(${data.measureType})` : ''}`
    }
    
    // 血压数据
    if (data.systolic && data.diastolic) {
      return `${data.systolic}/${data.diastolic} mmHg`
    }
    
    // 体重、身高等
    if (data.value && data.unit) {
      return `${data.value} ${data.unit}`
    }
    
    return JSON.stringify(data)
  } catch (e) {
    return JSON.stringify(data)
  }
}

// 获取思考内容预览
const getThinkingPreview = (thinking) => {
  if (!thinking) return ''
  const text = thinking.replace(/\n/g, ' ').trim()
  return text.length > 50 ? text.substring(0, 50) + '...' : text
}

// 获取思考过程标签
const getThinkingLabel = (message) => {
  if (message.thinking && message.thinking.trim()) {
    return '思考过程'
  }
  if (message.status && message.status.includes('思考')) {
    return '正在思考'
  }
  if (message.status && message.status.includes('分析')) {
    return '正在分析'
  }
  return '思考过程'
}

// 判断是否应该显示思考过程
const shouldShowThinking = (message) => {
  // 强制初始化thinking展开状态
  if (message.type === 'ai' && message._thinkingExpanded === undefined) {
    message._thinkingExpanded = false
  }
  
  // 自动修复：如果thinking为空但content中有thinking标签，尝试提取
  if (message.type === 'ai' && (!message.thinking || message.thinking.trim() === '') && 
      message.content && message.content.includes('<thinking>')) {
    extractThinkingFromFinalContent(message)
  }
  
  // 如果有思考内容，总是显示
  if (message.thinking && message.thinking.trim()) {
    return true
  }
  // 如果是AI消息且正在流式思考中，显示
  if (message.type === 'ai' && message._streamState?.inThinking) {
    return true
  }
  // 如果是AI消息且状态是思考相关，也显示（实时思考状态）
  if (message.type === 'ai' && message.status && (
    message.status.includes('思考') || 
    message.status.includes('分析') ||
    message.status === '正在准备...' ||
    message.status === '正在处理...'
  )) {
    return true
  }
  // 如果是AI消息且曾经设置过_thinkingExpanded，说明之前有思考内容
  if (message.type === 'ai' && message._thinkingExpanded !== undefined) {
    return true
  }
  
  // 对于所有AI消息都显示thinking区域（让用户可以看到思考过程）
  if (message.type === 'ai') {
    return true
  }
  
  return false
}

// 实时处理流式内容（包含思考过程）
const processStreamContent = (message, chunk) => {
  // 如果消息还没有流式状态，初始化
  if (!message._streamState) {
    message._streamState = {
      inThinking: false,
      thinkingBuffer: '',
      contentBuffer: ''
    }
  }
  
  const state = message._streamState
  let remainingChunk = chunk
  
  while (remainingChunk.length > 0) {
    if (!state.inThinking) {
      // 检查是否开始thinking
      const thinkingStartIndex = remainingChunk.indexOf('<thinking>')
      if (thinkingStartIndex !== -1) {
        // 添加thinking开始前的内容到正文
        if (thinkingStartIndex > 0) {
          const beforeThinking = remainingChunk.substring(0, thinkingStartIndex)
          state.contentBuffer += beforeThinking
          message.content = state.contentBuffer
        }
        
        // 进入thinking模式
        state.inThinking = true
        if (!message.thinking) {
          message.thinking = ''
          message._thinkingExpanded = false
        }
        
        // 处理thinking标签后的内容
        remainingChunk = remainingChunk.substring(thinkingStartIndex + '<thinking>'.length)
      } else {
        // 没有thinking标签，直接添加到正文
        state.contentBuffer += remainingChunk
        message.content = state.contentBuffer
        break
      }
    } else {
      // 在thinking模式中
      const thinkingEndIndex = remainingChunk.indexOf('</thinking>')
      if (thinkingEndIndex !== -1) {
        // 找到thinking结束
        const thinkingPart = remainingChunk.substring(0, thinkingEndIndex)
        state.thinkingBuffer += thinkingPart
        message.thinking = state.thinkingBuffer
        
        // 退出thinking模式
        state.inThinking = false
        remainingChunk = remainingChunk.substring(thinkingEndIndex + '</thinking>'.length)
      } else {
        // thinking还没结束，继续积累
        state.thinkingBuffer += remainingChunk
        message.thinking = state.thinkingBuffer
        break
      }
    }
  }
}

// 从完整内容中提取thinking（备用方法）
const extractThinkingFromFinalContent = (message) => {
  const content = message.content
  if (!content) return
  
  // 查找thinking标签
  const thinkingMatch = content.match(/<thinking>([\s\S]*?)<\/thinking>/i)
  if (thinkingMatch) {
    const thinkingContent = thinkingMatch[1].trim()
    
    if (thinkingContent) {
      message.thinking = thinkingContent
      message._thinkingExpanded = false
      
      // 从主内容中移除thinking部分
      message.content = content.replace(/<thinking>[\s\S]*?<\/thinking>/gi, '').trim()
    }
  }
}

// 工具操作处理
const handleToolAction = (action, toolCall) => {

  // 根据不同的action执行相应操作
  switch(action) {
    case 'viewDetail':
      // 查看详情
      break
    case 'refresh':
      // 刷新数据
      break
    case 'export':
      // 导出数据
      break
    default:
        console.warn('未知操作:', action)
  }
}

// 健康记录添加结果相关方法
const toggleRecordDetails = (toolCall) => {
  toolCall._expanded = !toolCall._expanded
}

const getAddRecordIcon = (toolCall) => {
  const recordData = parseRecordData(toolCall)
  if (!recordData) return '✅'
  
  // 根据新的数据结构获取记录类型
  const recordType = recordData.record_details?.type || recordData.record_type
  const icons = {
    'glucose': '🩸',
    'pressure': '🫀', 
    'weight': '⚖️'
  }
  return icons[recordType] || '📊'
}

const getAddRecordTitle = (toolCall) => {
  const recordData = parseRecordData(toolCall)
  if (!recordData) return '✅ 添加完成'
  
  const recordType = recordData.record_details?.type || recordData.record_type
  const typeNames = {
    'glucose': '血糖记录',
    'pressure': '血压记录',
    'weight': '体重记录'
  }
  const typeName = typeNames[recordType] || recordType
  return `${getAddRecordIcon(toolCall)} ${typeName}添加成功`
}

const getAddRecordValue = (toolCall) => {
  const recordData = parseRecordData(toolCall)
  if (!recordData) return ''
  
  try {
    // 从新的数据结构中获取值
    const data = recordData.record_details?.data
    const recordType = recordData.record_details?.type
    
    if (!data) return recordData.message || '已添加'
    
    switch(recordType) {
      case 'glucose':
        return `${data.value} ${data.unit || 'mmol/L'}`
      case 'pressure':
        return `${data.systolic}/${data.diastolic} ${data.unit || 'mmHg'}`
      case 'weight':
        return `${data.weight || data.value} ${data.unit || 'kg'}`
      default:
        return recordData.message || '已添加'
    }
  } catch (e) {
    return recordData.message || '已添加'
  }
}

const getAddRecordDetails = (toolCall) => {
  const recordData = parseRecordData(toolCall)
  if (!recordData) return []
  
  const details = []
  
  // 用户信息
  if (recordData.user_info) {
    details.push({
      label: '用户',
      value: `${recordData.user_info.real_name} (ID: ${recordData.user_info.user_id})`,
      type: 'text'
    })
  }
  
  // 消息
  if (recordData.message) {
    details.push({
      label: '状态',
      value: recordData.message,
      type: 'text'
    })
  }
  
  // 记录类型
  if (recordData.record_details) {
  const typeNames = {
    'glucose': '血糖记录',
    'pressure': '血压记录',
    'weight': '体重记录'
  }
  details.push({
    label: '记录类型',
      value: typeNames[recordData.record_details.type] || recordData.record_details.type,
    type: 'text'
  })
  
  // 记录详情
      details.push({
        label: '记录详情',
      value: JSON.stringify(recordData.record_details.data, null, 2),
        type: 'json'
      })
    
    // 总记录数
    if (recordData.record_details.total_records_count) {
      details.push({
        label: '总记录数',
        value: recordData.record_details.total_records_count,
        type: 'text'
      })
    }
  }
  
  // 操作时间
  if (recordData.operation_time) {
    details.push({
      label: '操作时间',
      value: formatDateTime(recordData.operation_time),
      type: 'text'
    })
  }
  
  return details
}

const parseRecordData = (toolCall) => {
  // 使用统一的解析函数
  return parseToolResult(toolCall)
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  try {
    return new Date(dateStr).toLocaleString('zh-CN')
  } catch (e) {
    return dateStr
  }
}

// WebSocket连接管理
const connectWebSocket = () => {
  try {
    const token = userStore.token
    if (!token) {
      ElMessage.error('请先登录后使用智能问诊功能')
      return
    }

    // 创建WebSocket连接
    const wsUrl = `ws://localhost:8081/ws/chat`
    websocket.value = new WebSocket(wsUrl)
    
    websocket.value.onopen = () => {
      console.log('WebSocket连接已建立')
      isConnected.value = true
    }
    
    websocket.value.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        handleWebSocketMessage(data)
      } catch (e) {
        console.error('解析WebSocket消息失败:', e)
      }
    }
    
    websocket.value.onclose = () => {
      console.log('WebSocket连接已关闭')
      isConnected.value = false
      // 可以考虑自动重连
    }
    
    websocket.value.onerror = (error) => {
      console.error('WebSocket错误:', error)
      isConnected.value = false
      ElMessage.error('实时连接失败，请刷新页面重试')
    }
    
  } catch (error) {
    console.error('建立WebSocket连接失败:', error)
    ElMessage.error('无法建立实时连接')
  }
}

const handleWebSocketMessage = (data) => {
  // 严格检查：必须有有效的currentAiMessage才处理
  if (!currentAiMessage.value || !currentAiMessage.value.id) {
    console.warn('收到WebSocket消息但没有有效的currentAiMessage:', data.type)
    return
  }
  
  switch (data.type) {
    case 'start':
      // 开始处理 - 只更新状态
      currentAiMessage.value.status = '正在分析...'
      break
      
    // =================== 多专家模式事件 ===================
    case 'planning_start':
      // 规划开始
      currentAiMessage.value.status = data.message || '🧠 正在分析问题并制定执行计划...'
      currentAiMessage.value.expertPlan = {
        stage: 'planning',
        experts: [],
        currentExpert: null
      }
      break
      
    case 'plan_created':
      // 计划创建完成
      const tasks = data.tasks || (data.plan?.tasks) || []
      const taskCount = tasks.length || (data.plan?.experts?.length || 0)
      currentAiMessage.value.status = data.message || `📋 执行计划：将依次执行 ${taskCount} 个任务`
      currentAiMessage.value.expertPlan = {
        stage: 'executing',
        plan: data.plan,
        reasoning: data.reasoning || (data.plan?.reasoning),
        tasks: tasks,  // 新增：任务列表
        experts: tasks.map((task, index) => ({
          name: task.expert || task,
          taskDescription: task.task_description || '',  // 新增：任务描述
          status: 'pending',
          result: null,
          _analysisExpanded: false  // 初始化展开状态
        })),
        currentExpertIndex: -1
      }
      break
      
    case 'expert_start':
      // 专家开始执行
      if (currentAiMessage.value.expertPlan) {
        const index = currentAiMessage.value.expertPlan.experts.findIndex(e => e.name === data.expert)
        if (index !== -1) {
          currentAiMessage.value.expertPlan.experts[index].status = 'running'
          currentAiMessage.value.expertPlan.experts[index].taskDescription = data.task_description || currentAiMessage.value.expertPlan.experts[index].taskDescription  // 更新任务描述
          currentAiMessage.value.expertPlan.currentExpertIndex = index
        }
      }
      currentAiMessage.value.status = data.message || `🤖 正在咨询 ${data.expert}...`
      break
      
    case 'expert_complete':
      // 专家完成
      if (currentAiMessage.value.expertPlan) {
        const index = currentAiMessage.value.expertPlan.experts.findIndex(e => e.name === data.expert)
        if (index !== -1) {
          // 修复：success 在 data.result 里面，不是在 data 顶层
          const isSuccess = data.result?.success !== false  // 默认为 true
          currentAiMessage.value.expertPlan.experts[index].status = isSuccess ? 'completed' : 'error'
          currentAiMessage.value.expertPlan.experts[index].success = isSuccess
          currentAiMessage.value.expertPlan.experts[index].result = data.result || {}
          currentAiMessage.value.expertPlan.experts[index].completionReport = data.completion_report || data.report || data.result?.completion_report || ''
          currentAiMessage.value.expertPlan.experts[index].assignedTask = data.task || currentAiMessage.value.expertPlan.experts[index].taskDescription
          currentAiMessage.value.expertPlan.experts[index]._analysisExpanded = false // 初始化为折叠状态
          currentAiMessage.value.expertPlan.experts[index]._detailsExpanded = false // 初始化详情为折叠状态
          
          // 新增：处理 ReAct 信息
          if (data.react_info) {
            currentAiMessage.value.expertPlan.experts[index].reactInfo = {
              iterations: data.react_info.iterations || 0,
              goalAchieved: data.react_info.goal_achieved || false,
              steps: (data.react_info.steps || []).map(step => ({
                stepNum: step.step,
                thought: step.thought,
                action: step.action,
                observation: step.observation,
                reflection: step.reflection,
                _expanded: false // 默认折叠
              }))
            }
            currentAiMessage.value.expertPlan.experts[index]._reactExpanded = false // ReAct 详情默认折叠
          }
          
          // 初始化MCP调用的展开状态
          if (data.result?.mcp_calls && Array.isArray(data.result.mcp_calls)) {
            data.result.mcp_calls.forEach(mcpCall => {
              mcpCall._expanded = false // 默认折叠
              mcpCall._showRaw = false // 默认显示格式化视图
            })
          }
        }
      }
      currentAiMessage.value.status = data.message || `✅ ${data.expert} 分析完成`
      break
      
    case 'expert_error':
      // 专家执行错误
      if (currentAiMessage.value.expertPlan) {
        const index = currentAiMessage.value.expertPlan.experts.findIndex(e => e.name === data.expert)
        if (index !== -1) {
          currentAiMessage.value.expertPlan.experts[index].status = 'error'
          currentAiMessage.value.expertPlan.experts[index].error = data.error
        }
      }
      break
      
    case 'thinking':
      // AI思考过程 - 实时流式追加到思考内容（如果后端发送了专门的thinking消息）
      if (!currentAiMessage.value.thinking) {
        currentAiMessage.value.thinking = ''
        currentAiMessage.value._thinkingExpanded = false
      }
      currentAiMessage.value.thinking += data.content
      currentAiMessage.value.status = ''
      break
      
    case 'content':
      // 流式内容 - 实时处理思考过程
      processStreamContent(currentAiMessage.value, data.content)
      
      // 备用：如果流式处理没有提取到thinking，尝试传统方式
      if (!currentAiMessage.value.thinking && currentAiMessage.value.content.includes('<thinking>')) {
        extractThinkingFromFinalContent(currentAiMessage.value)
      }
      break
      
    case 'tool_call_start':
      // 工具调用开始
      currentAiMessage.value.status = '正在准备工具...'
      break
      
    case 'tools_start':
      currentAiMessage.value.status = data.message || '正在调用工具...'
      break
      
    case 'tool_executing':
      currentAiMessage.value.status = `正在调用 ${data.tool_name}...`
      break
      
    case 'tool_complete':
      if (!currentAiMessage.value.toolCalls) {
        currentAiMessage.value.toolCalls = []
      }
      
      const toolCallData = {
        tool_name: data.tool_name,
        result: data.result,
        _expanded: false
      }
      
      currentAiMessage.value.toolCalls.push(toolCallData)
      break
      
    case 'final_response_start':
      // 最终回复开始 - 清空内容重新开始
      currentAiMessage.value.status = data.message || '正在生成回复...'
      currentAiMessage.value.content = ''
      break
      
    case 'final_content':
      // 最终内容 - 实时处理思考过程
      processStreamContent(currentAiMessage.value, data.content)
      
      // 备用：如果流式处理没有提取到thinking，尝试传统方式
      if (!currentAiMessage.value.thinking && currentAiMessage.value.content.includes('<thinking>')) {
        extractThinkingFromFinalContent(currentAiMessage.value)
      }
      break
      
    case 'complete':
      // 完成 - 只清理状态，标记完成
      const completingMessageId = currentAiMessage.value.id
      
      if (data.final_response) {
        // 保存已有的thinking内容，避免被覆盖
        const existingThinking = currentAiMessage.value.thinking
        
        // 只更新content，保留thinking
        currentAiMessage.value.content = data.final_response
        
        // 如果没有thinking内容，尝试从最终回复中提取
        if (!existingThinking) {
          extractThinkingFromFinalContent(currentAiMessage.value)
        } else {
          // 恢复已有的thinking内容
          currentAiMessage.value.thinking = existingThinking
          // 清理content中可能的thinking标签
          currentAiMessage.value.content = currentAiMessage.value.content.replace(/<thinking>[\s\S]*?<\/thinking>/gi, '').trim()
        }
      }
      if (data.tool_calls) {
        // 如果complete事件中有工具调用结果，优先使用它们（通常更完整）
        currentAiMessage.value.toolCalls = data.tool_calls.map(tc => ({
          ...tc,
          _expanded: false
        }))
      }
      
      // 清理状态标记
      delete currentAiMessage.value.status
      delete currentAiMessage.value.streaming
      currentAiMessage.value.completed = true
      
      // 重置引用和加载状态
      currentAiMessage.value = null
      isLoading.value = false
      break
      
    case 'error':
      // 错误处理 - 只更新现有消息
      const errorMessageId = currentAiMessage.value.id
      
      ElMessage.error(`AI回复失败: ${data.error}`)
      currentAiMessage.value.content = `抱歉，处理您的消息时出现错误：${data.error}`
      currentAiMessage.value.error = true
      delete currentAiMessage.value.status
      delete currentAiMessage.value.streaming
      currentAiMessage.value.completed = true
      
      currentAiMessage.value = null
      isLoading.value = false
      break
      
    default:
      console.warn('未知的WebSocket消息类型:', data.type)
  }
}

const disconnectWebSocket = () => {
  if (websocket.value) {
    websocket.value.close()
    websocket.value = null
    isConnected.value = false
  }
}

// 创建markdown解析器实例
const md = new MarkdownIt({
  html: false,        // 禁用HTML标签解析（安全考虑）
  xhtmlOut: false,    // 使用HTML5标准
  breaks: true,       // 换行符转换为<br>
  linkify: true,      // 自动识别URL
  typographer: true   // 启用一些语言中性的替换 + 引号美化
})

// 响应式数据
const messages = ref([])
const currentMessage = ref('')
const isLoading = ref(false)
const chatContainer = ref(null)
const websocket = ref(null)
const isConnected = ref(false)
const currentAiMessage = ref(null)

// 会话管理相关
const sessionList = ref([])
const currentSessionId = ref(null)
const currentSessionTitle = computed(() => {
  if (messages.value.length === 0) {
    return '新对话'
  }
  // 使用第一条用户消息作为标题
  const firstUserMessage = messages.value.find(m => m.type === 'user')
  if (firstUserMessage) {
    return firstUserMessage.content.substring(0, 20) + (firstUserMessage.content.length > 20 ? '...' : '')
  }
  return '对话中'
})

// 快捷问题
const quickQuestions = ref([
  '我想查看我最近的血糖记录',
  '帮我推荐一位内分泌科医生',
  '我的血压控制得怎么样？',
  '我想了解我的咨询历史',
  '根据我的症状推荐合适的医生',
  '我的健康数据有什么趋势？'
])

// 初始化组件状态
const initializeComponent = () => {
  console.log('初始化AI问诊组件状态')
  
  // 清理任何遗留的状态
  currentAiMessage.value = null
  isLoading.value = false
  
  // 清理消息数组中的异常AI消息
  const beforeCount = messages.value.length
  messages.value = messages.value.filter(msg => {
    if (msg.type === 'ai' && msg.streaming && !msg.content && !msg.completed) {
      console.log('清理异常AI消息:', msg.id)
      return false
    }
    return true
  })
  
  const afterCount = messages.value.length
  if (beforeCount !== afterCount) {
    console.log(`清理完成，消息数量从${beforeCount}减少到${afterCount}`)
  }
}

// 页面初始化
onMounted(async () => {
  console.log('AI智能问诊页面已加载')
  
  // 首先初始化清理组件状态
  initializeComponent()
  
  // 检查用户认证状态
  if (!userStore.isAuthenticated) {
    ElMessage.error('请先登录后使用智能问诊功能')
    return
  }
  
  // 加载历史对话记录
  await loadChatHistory()
  
  // 加载会话列表
  await loadSessions()
  
  // 建立WebSocket连接
  connectWebSocket()
  
  // 测试Agent后端连接
  try {
    const healthResponse = await agentAPI.healthCheck()
    if (healthResponse.success || healthResponse.status === 'healthy') {
      console.log('Agent后端连接正常')
    }
  } catch (error) {
    console.warn('Agent后端连接失败:', error)
    ElMessage.warning('AI服务连接异常，可能影响智能问诊功能')
  }
})

// 页面卸载时断开WebSocket连接
onUnmounted(() => {
  disconnectWebSocket()
})

// 发送消息（使用WebSocket流式对话）
const sendMessage = async () => {
  if (!currentMessage.value.trim() || isLoading.value) {
    return
  }

  // 检查WebSocket连接
  if (!isConnected.value || !websocket.value) {
    ElMessage.error('连接已断开，正在重新连接...')
    connectWebSocket()
    return
  }

  // 关键修复：清理任何未完成的AI消息，防止重复
  cleanupIncompleteAiMessage()

  const userMessage = {
    id: `user_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    type: 'user',
    content: currentMessage.value.trim(),
    timestamp: new Date()
  }

  // 添加用户消息
  messages.value.push(userMessage)
  
  // 清空输入框
  const messageToSend = currentMessage.value.trim()
  currentMessage.value = ''
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 开始AI回复 - 确保全局状态正确
  isLoading.value = true
  
  // 延迟创建AI消息，确保上一条消息已完全处理
  await nextTick()

  // 创建唯一的AI响应消息
  const aiMessage = {
    id: `ai_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
    type: 'ai', 
    content: '',
    thinking: '',
    timestamp: new Date(),
    toolCalls: [],
    streaming: true,
    status: '正在思考...',
    _thinkingExpanded: false,
    _expertPlanExpanded: true, // 默认展开专家计划
    expertPlan: null, // 多专家计划
    _streamState: {
      inThinking: false,
      thinkingBuffer: '',
      contentBuffer: ''
    }
  }

  // 确保没有重复添加
  messages.value.push(aiMessage)
  currentAiMessage.value = aiMessage
  
  console.log('创建AI消息，当前消息总数:', messages.value.length, 'AI消息ID:', aiMessage.id)

  try {
    // 通过WebSocket发送消息（包含session_id）
    const messageData = {
      message: messageToSend,
      token: userStore.token,
      session_id: currentSessionId.value || null  // 包含当前会话ID
    }
    
    console.log('发送WebSocket消息，会话ID:', currentSessionId.value)
    websocket.value.send(JSON.stringify(messageData))
    
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败，请重试')
    
    // 错误处理：移除失败的AI消息
    removeIncompleteAiMessage()
    isLoading.value = false
  }
}

// 新增：清理未完成的AI消息的辅助函数
const cleanupIncompleteAiMessage = () => {
  // 如果有正在进行的AI消息，先清理掉
  if (currentAiMessage.value) {
    const index = messages.value.findIndex(m => m.id === currentAiMessage.value.id)
    if (index > -1) {
      messages.value.splice(index, 1)
      console.log('清理了未完成的AI消息')
    }
    currentAiMessage.value = null
  }
  
  // 额外安全措施：移除任何状态不完整的AI消息
  messages.value = messages.value.filter(msg => {
    if (msg.type === 'ai' && msg.streaming && !msg.content && !msg.completed) {
      console.log('移除状态异常的AI消息:', msg.id)
      return false
    }
    return true
  })
}

// 新增：移除失败的AI消息
const removeIncompleteAiMessage = () => {
  if (currentAiMessage.value) {
    const index = messages.value.findIndex(m => m.id === currentAiMessage.value.id)
    if (index > -1) {
      messages.value.splice(index, 1)
    }
    currentAiMessage.value = null
  }
}

// 加载对话历史
const loadChatHistory = async () => {
  try {
    const response = await agentAPI.getChatHistory(20)
    
    if (response.success && response.messages) {
      // 转换后端消息格式到前端格式
      const convertedMessages = response.messages
        .filter(msg => msg.role !== 'system') // 排除系统消息
        .map((msg, index) => {
          const message = {
          id: `history_${msg.role}_${Date.now()}_${index}_${Math.random().toString(36).substr(2, 9)}`,
          type: msg.role === 'user' ? 'user' : 'ai',
          content: msg.content,
          timestamp: new Date(msg.timestamp),
          completed: true,  // 历史消息都是完整的
          toolCalls: (msg.tool_calls || []).map(tc => ({
            ...tc,
            _expanded: false  // 初始化展开状态
          })),
          _expertPlanExpanded: false // 历史消息默认折叠专家计划
          }
          
          // 如果是AI消息且有thinking字段，添加thinking内容
          if (msg.role === 'assistant' && msg.thinking) {
            message.thinking = msg.thinking
            message._thinkingExpanded = false  // 初始化thinking展开状态
            console.log('📚 加载历史消息时发现thinking:', msg.thinking.substring(0, 50) + '...')
          } else if (msg.role === 'assistant') {
            console.log('📚 加载AI历史消息但无thinking字段:', {
              hasThinking: !!msg.thinking,
              contentHasThinking: msg.content?.includes('<thinking>'),
              content: msg.content?.substring(0, 100) + '...'
            })
          }
          
          // 如果是AI消息且有expert_plan字段，添加专家计划信息
          if (msg.role === 'assistant' && msg.expert_plan) {
            const expertPlan = msg.expert_plan
            const tasks = expertPlan.tasks || []
            
            message.expertPlan = {
              plan: expertPlan.plan || {},
              reasoning: expertPlan.reasoning || expertPlan.plan?.reasoning,  // 添加推理
              tasks: tasks,  // 添加任务列表
              experts: (expertPlan.experts || []).map((expert, index) => {
                // 从tasks中找到对应的任务描述
                const task = tasks.find(t => t.expert === expert.name) || tasks[index]
                
                // 初始化MCP调用的展开状态
                if (expert.result?.mcp_calls && Array.isArray(expert.result.mcp_calls)) {
                  console.log(`📌 专家 ${expert.name} 有 ${expert.result.mcp_calls.length} 个MCP调用`)
                  console.log('📌 第一个MCP调用:', expert.result.mcp_calls[0])
                  expert.result.mcp_calls.forEach(mcpCall => {
                    mcpCall._expanded = false // 默认折叠
                    mcpCall._showRaw = false // 默认显示格式化视图
                  })
                } else {
                  console.log(`⚠️ 专家 ${expert.name} 没有mcp_calls数据`, {
                    hasResult: !!expert.result,
                    hasMcpCalls: !!expert.result?.mcp_calls,
                    resultKeys: expert.result ? Object.keys(expert.result) : []
                  })
                }
                
                // 从 expert.react_info (WebSocket) 或 expert.result (MongoDB) 中提取 ReAct 信息
                const reactSource = expert.react_info || expert.result
                const hasReactInfo = reactSource?.react_mode && reactSource?.steps
                
                // 如果 success 未定义，默认为 true（从数据库加载的都应该是成功的）
                const isSuccess = expert.success !== false
                
                return {
                  name: expert.name,
                  success: isSuccess,
                  status: isSuccess ? 'completed' : 'error',  // 添加status字段
                  result: expert.result,
                  taskDescription: task?.task_description || expert.result?.assigned_task || '',  // 任务描述
                  assignedTask: expert.result?.assigned_task || '',  // 分配的任务
                  completionReport: expert.result?.completion_report || '',  // 完成汇报
                  _analysisExpanded: false,  // 初始化专家分析展开状态
                  _detailsExpanded: false,  // 初始化详情折叠状态
                  reactInfo: hasReactInfo ? {
                    iterations: reactSource.iterations || 0,
                    goalAchieved: reactSource.goal_achieved || reactSource.goalAchieved || false,
                    steps: (reactSource.steps || []).map(step => ({
                      stepNum: step.step || step.stepNum,
                      thought: step.thought,
                      action: step.action,
                      observation: step.observation,
                      reflection: step.reflection,
                      _expanded: false
                    }))
                  } : null,
                  _reactExpanded: false  // ReAct 详情默认折叠
                }
              })
            }
            console.log('📚 加载历史消息时发现expert_plan:', expertPlan.experts?.length, '个专家')
          }
          
          return message
        })
      
      messages.value = convertedMessages
      
      if (convertedMessages.length > 0) {
        console.log(`已加载 ${convertedMessages.length} 条历史对话`)
        await nextTick()
        scrollToBottom()
      }
    }
  } catch (error) {
    console.warn('加载对话历史失败:', error)
    // 不显示错误消息，静默失败
  }
}

// 选择快捷问题
const selectQuickQuestion = (question) => {
  currentMessage.value = question
  sendMessage()
}

// 清空对话
const clearChat = async () => {
  try {
    // 调用后端API清空对话历史
    const response = await agentAPI.clearChatHistory()
    
    if (response.success) {
      messages.value = []
      ElMessage.success('对话历史已清空')
    } else {
      ElMessage.error('清空对话失败：' + (response.error || '未知错误'))
    }
  } catch (error) {
    console.error('清空对话失败:', error)
    // 即使后端调用失败，也清空前端显示
    messages.value = []
    ElMessage.warning('本地对话已清空，服务器同步可能失败')
  }
}



// 格式化消息内容
const formatMessage = (content) => {
  try {
    if (!content || typeof content !== 'string') {
      return content || ''
    }
    // 使用markdown-it解析markdown格式
    return md.render(content)
  } catch (error) {
    console.error('Markdown解析错误:', error)
    // 降级处理：如果markdown解析失败，使用原来的简单处理方式
  return content.replace(/\n/g, '<br>').replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

// 获取工具显示名称
const getToolDisplayName = (toolName) => {
  const toolNames = {
    'query_user_health_records': '健康记录查询',
    'add_health_record': '健康记录添加',
    'query_doctor_list': '医生列表查询',
    'query_user_consultations': '咨询记录查询', 
    'query_department_info': '科室信息查询',
    'query_system_overview': '系统概览查询',
    'search_doctors_by_condition': '医生推荐',
    'search_diabetes_knowledge': '糖尿病知识检索',
    'rag_health_check': 'RAG健康检查',
    'get_diabetes_knowledge_categories': '知识库分类查询',
    'clear_rag_cache': '清理RAG缓存'
  }
  return toolNames[toolName] || toolName
}

// 获取记录类型名称
const getRecordTypeName = (type) => {
  const typeNames = {
    'glucose': '血糖记录',
    'pressure': '血压记录', 
    'weight': '体重记录'
  }
  return typeNames[type] || type
}

// 重新加载对话历史
const refreshHistory = async () => {
  const loading = ElLoading.service({
    lock: true,
    text: '正在同步对话历史...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  try {
    await loadChatHistory()
    ElMessage.success('对话历史已同步')
  } catch (error) {
    ElMessage.error('同步失败：' + error.message)
  } finally {
    loading.close()
  }
}

// 加载会话列表
const loadSessions = async () => {
  try {
    const response = await agentAPI.getChatSessions(10)
    if (response.success) {
      sessionList.value = response.sessions || []
      currentSessionId.value = response.current_session_id
      console.log(`已加载 ${sessionList.value.length} 个会话`)
    }
  } catch (error) {
    console.warn('加载会话列表失败:', error)
  }
}

// 刷新会话列表
const refreshSessions = async () => {
  await loadSessions()
  ElMessage.success('会话列表已刷新')
}

// 处理会话命令
const handleSessionCommand = async (command) => {
  if (command === 'new') {
    await createNewSession()
  } else if (command.startsWith('switch:')) {
    const sessionId = command.replace('switch:', '')
    await switchSession(sessionId)
  }
}

// 创建新会话
const createNewSession = async () => {
  const loading = ElLoading.service({
    lock: true,
    text: '正在创建新会话...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  try {
    const response = await agentAPI.createNewSession()
    if (response.success) {
      // 清空当前消息
      messages.value = []
      currentSessionId.value = response.session_id
      
      // 重新加载会话列表
      await loadSessions()
      
      ElMessage.success('新会话已创建')
    }
  } catch (error) {
    console.error('创建新会话失败:', error)
    ElMessage.error('创建新会话失败：' + error.message)
  } finally {
    loading.close()
  }
}

// 切换会话
const switchSession = async (sessionId) => {
  if (sessionId === currentSessionId.value) {
    ElMessage.info('已经是当前会话')
    return
  }
  
  const loading = ElLoading.service({
    lock: true,
    text: '正在切换会话...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  try {
    const response = await agentAPI.switchChatSession(sessionId)
    if (response.success) {
      currentSessionId.value = response.session_id
      
      // 转换消息格式
      const convertedMessages = response.messages
        .filter(msg => msg.role !== 'system')
        .map((msg, index) => {
          const message = {
            id: `history_${msg.role}_${Date.now()}_${index}_${Math.random().toString(36).substr(2, 9)}`,
            type: msg.role === 'user' ? 'user' : 'ai',
            content: msg.content,
            timestamp: new Date(msg.timestamp),
            completed: true,
            toolCalls: (msg.tool_calls || []).map(tc => ({
              ...tc,
              _expanded: false
            })),
            _expertPlanExpanded: false
          }
          
          if (msg.role === 'assistant' && msg.thinking) {
            message.thinking = msg.thinking
            message._thinkingExpanded = false
          }
          
          if (msg.role === 'assistant' && msg.expert_plan) {
            const expertPlan = msg.expert_plan
            const tasks = expertPlan.tasks || []
            
            message.expertPlan = {
              plan: expertPlan.plan || {},
              reasoning: expertPlan.reasoning || expertPlan.plan?.reasoning,
              tasks: tasks,
              experts: (expertPlan.experts || []).map((expert, index) => {
                const task = tasks.find(t => t.expert === expert.name) || tasks[index]
                
                if (expert.result?.mcp_calls) {
                  expert.result.mcp_calls.forEach(mcpCall => {
                    mcpCall._expanded = false
                    mcpCall._showRaw = false
                  })
                }
                
                // 如果 success 未定义，默认为 true
                const isSuccess = expert.success !== false
                
                // 检查 ReAct 信息：可能在 expert.react_info 或 expert.result 中
                const reactSource = expert.react_info || expert.result
                const hasReactInfo = reactSource && (reactSource.iterations !== undefined || reactSource.steps)
                
                return {
                  name: expert.name,
                  success: isSuccess,
                  status: isSuccess ? 'completed' : 'error',
                  result: expert.result,
                  taskDescription: task?.task_description || expert.result?.assigned_task || '',
                  assignedTask: expert.result?.assigned_task || '',
                  completionReport: expert.result?.completion_report || '',
                  _analysisExpanded: false,
                  _detailsExpanded: false,
                  reactInfo: hasReactInfo ? {
                    iterations: reactSource.iterations || 0,
                    goalAchieved: reactSource.goal_achieved || reactSource.goalAchieved || false,
                    steps: (reactSource.steps || []).map(step => ({
                      stepNum: step.step || step.stepNum,
                      thought: step.thought,
                      action: step.action,
                      observation: step.observation,
                      reflection: step.reflection,
                      _expanded: false
                    }))
                  } : null,
                  _reactExpanded: false
                }
              })
            }
          }
          
          return message
        })
      
      messages.value = convertedMessages
      
      // 滚动到底部
      await nextTick()
      scrollToBottom()
      
      ElMessage.success('已切换到该会话')
    }
  } catch (error) {
    console.error('切换会话失败:', error)
    ElMessage.error('切换会话失败：' + error.message)
  } finally {
    loading.close()
  }
}

// 格式化会话时间
const formatSessionTime = (time) => {
  if (!time) return ''
  
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  // 今天
  if (diff < 24 * 60 * 60 * 1000) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  
  // 昨天
  if (diff < 48 * 60 * 60 * 1000) {
    return '昨天'
  }
  
  // 本周
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = ['日', '一', '二', '三', '四', '五', '六']
    return '周' + days[date.getDay()]
  }
  
  // 更早
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}
</script>

<style scoped>
.ai-consultation {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.ai-consultation-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  background: #ffffff;
}

/* 空状态样式 - 极简风格 */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  overflow-y: auto;
  min-height: 0;
  position: relative;
}

/* 空状态历史记录按钮 */
.empty-history-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 100;
}

.empty-history-btn .el-button {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  border: 1px solid #e5e7eb;
  transition: all 0.2s ease;
}

.empty-history-btn .el-button:hover {
  background: #f9fafb;
  border-color: #667eea;
  color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.empty-content {
  max-width: 750px;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: fadeInUp 0.5s ease-out;
}

.brand-header {
  margin-bottom: 48px;
}

.brand-title {
  font-size: 32px;
  font-weight: 600;
  margin: 0;
  color: #1f2937;
  letter-spacing: 0.5px;
}

/* 空状态输入区域 */
.empty-input-section {
  width: 100%;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.brand-header {
  text-align: center;
  margin-bottom: 0;
}

.brand-title {
  font-size: 32px;
  font-weight: 600;
  margin: 0;
  color: #1f2937;
  letter-spacing: 0.5px;
}


/* 对话状态样式 */
.ai-consultation-content.has-messages {
  background: linear-gradient(to bottom, #fafbfc 0%, #ffffff 100%);
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  animation: fadeIn 0.3s ease-out;
  display: flex;
  flex-direction: column;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 浮动会话管理按钮 */
.chat-session-btns {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 100;
  display: flex;
  gap: 8px;
}

.chat-session-btns .session-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  border: 1px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
}

.chat-session-btns .session-btn:hover {
  background: #f9fafb;
  border-color: #667eea;
  color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
  transform: translateY(-1px);
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 280px;
  padding: 8px 0;
}

.session-info {
  flex: 1;
  overflow: hidden;
}

.session-preview {
  font-size: 14px;
  color: #374151;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.session-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #9ca3af;
}

.session-check {
  color: #667eea;
  font-size: 16px;
  margin-left: 8px;
}

.is-active {
  background-color: #f3f4f6;
}

.chat-messages {
  max-width: 900px;
  width: 100%;
  margin: 0 auto;
  padding: 24px 20px;
  flex: 1;
}

.ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 12px;
  flex-shrink: 0;
}

.welcome-text {
  flex: 1;
}

.welcome-text h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 18px;
}

.welcome-text p {
  margin: 0 0 16px 0;
  color: #606266;
}

.disclaimer {
  margin-top: 16px;
}

.message-item {
  display: flex;
  margin-bottom: 24px;
  align-items: flex-start;
  animation: messageSlideIn 0.3s ease-out;
  width: 100%;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* AI消息：固定在左边 */
.message-item.ai {
  justify-content: flex-start;
}

.message-item.ai .message-content {
  margin-left: 12px;
}

/* 用户消息：固定在右边 */
.message-item.user {
  flex-direction: row-reverse;
  justify-content: flex-start;
}

.message-item.user .message-content {
  margin-right: 12px;
  margin-left: 0;
}

.message-avatar {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
}

.ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.message-content {
  max-width: 75%;
  min-width: 200px;
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 16px;
  position: relative;
  line-height: 1.6;
  word-break: break-word;
}

.message-item.user .message-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 6px;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.25);
}

.message-item.ai .message-bubble {
  background: white;
  color: #303133;
  border: 1px solid #e8eaed;
  border-bottom-left-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 多专家计划容器样式 - 极简风格 */
.expert-plan-container {
  margin: 14px 0;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
  background: white;
  transition: all 0.3s ease;
}

.expert-plan-container:hover {
  border-color: #e0e0e0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.expert-plan-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px;
  cursor: pointer;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.expert-plan-header:hover {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
}

.plan-icon {
  font-size: 20px;
  animation: pulse-icon 2s ease-in-out infinite;
}

@keyframes pulse-icon {
  0%, 100% { 
    transform: scale(1); 
    filter: brightness(1);
  }
  50% { 
    transform: scale(1.1); 
    filter: brightness(1.2);
  }
}

.plan-title {
  flex: 1;
  font-weight: 600;
  font-size: 15px;
  letter-spacing: 0.3px;
}

.plan-summary {
  font-size: 12px;
  font-weight: 500;
  opacity: 0.95;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 10px;
  border-radius: 12px;
  letter-spacing: 0.3px;
}

.expert-plan-content {
  padding: 18px 20px;
  background: #fefefe;
  animation: slideDown 0.3s ease-out;
  border-top: 1px solid #f0f0f0;
}

.plan-reasoning {
  margin-bottom: 16px;
  padding: 14px 16px;
  background: #fafbfc;
  border-left: 3px solid #667eea;
  border-radius: 8px;
  border: 1px solid #e8eaed;
  border-left-width: 3px;
  transition: all 0.2s ease;
}

.plan-reasoning:hover {
  background: #f5f7fa;
  border-color: #667eea;
  box-shadow: 0 1px 3px rgba(102, 126, 234, 0.1);
}

.reasoning-label {
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 10px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.reasoning-text {
  color: #374151;
  font-size: 13px;
  line-height: 1.6;
}

/* 专家时间线样式 */
.experts-timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.expert-item {
  display: flex;
  gap: 14px;
  position: relative;
  animation: expertSlideIn 0.4s ease-out;
}

@keyframes expertSlideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.expert-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.expert-number {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 15px;
  background: #e4e7ed;
  color: #909399;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.expert-item.pending .expert-number {
  background: #f5f7fa;
  color: #c0c4cc;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.expert-item.running .expert-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  animation: pulse-expert 1.5s infinite;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.expert-item.completed .expert-number {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.expert-item.error .expert-number {
  background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

@keyframes pulse-expert {
  0%, 100% { 
    transform: scale(1); 
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4), 0 0 0 0 rgba(102, 126, 234, 0.7); 
  }
  50% { 
    transform: scale(1.08); 
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.5), 0 0 0 8px rgba(102, 126, 234, 0); 
  }
}

.expert-line {
  width: 2px;
  flex: 1;
  background: linear-gradient(180deg, #e8eaed 0%, #f5f7fa 100%);
  margin-top: 6px;
  transition: all 0.3s;
}

.expert-item.running .expert-line,
.expert-item.completed .expert-line {
  background: linear-gradient(180deg, #667eea 0%, #e8eaed 100%);
}

.expert-content {
  flex: 1;
  padding: 14px 16px;
  background: #fefefe;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: none;
}

.expert-content:hover {
  background: #fafbfc;
  border-color: #e8eaed;
}

.expert-item.running .expert-content {
  background: #f8faff;
  border-color: #c7d2fe;
  border-left-width: 2px;
  box-shadow: none;
}

.expert-item.completed .expert-content {
  background: #f6fef9;
  border-color: #bbf7d0;
  border-left-width: 2px;
  box-shadow: none;
}

.expert-item.error .expert-content {
  background: #fef5f5;
  border-color: #fecaca;
  border-left-width: 2px;
  box-shadow: none;
}

/* 简化的专家头部 */
.expert-header-compact {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.expert-main-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.expert-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.expert-icon {
  font-size: 22px;
  line-height: 1;
}

.expert-name {
  flex: 1;
  font-weight: 600;
  color: #1f2937;
  font-size: 15px;
  letter-spacing: 0.2px;
}

/* 快速预览样式 */
.expert-summary {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 12px;
  background: #f9fafb;
  border-radius: 6px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
}

.summary-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.summary-text {
  flex: 1;
}

/* 详情面板样式 */
.expert-details-panel {
  margin-top: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.detail-section {
  margin-bottom: 12px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-label .el-icon {
  font-size: 14px;
}

.detail-label-with-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  padding: 6px 8px;
  margin: -6px -8px 6px;
  border-radius: 6px;
  transition: background 0.2s;
}

.detail-label-with-action:hover {
  background: #f3f4f6;
}

.label-with-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #4b5563;
}

.label-with-tags .el-icon {
  font-size: 14px;
}

.label-with-tags .el-tag {
  font-size: 11px;
}

/* MCP 调用交互式展示样式 */
.mcp-calls-interactive-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
}

.mcp-call-interactive-item {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s ease;
}

.mcp-call-interactive-item:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.mcp-call-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.mcp-call-header:hover {
  background: #f9fafb;
}

.mcp-call-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: white;
  font-size: 16px;
  flex-shrink: 0;
}

.mcp-call-info {
  flex: 1;
  min-width: 0;
}

.mcp-call-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.mcp-call-summary {
  font-size: 12px;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.expand-arrow {
  color: #9ca3af;
  transition: transform 0.2s ease;
  font-size: 16px;
  flex-shrink: 0;
}

.expand-arrow.rotated {
  transform: rotate(180deg);
}

.mcp-call-details {
  border-top: 1px solid #e5e7eb;
  padding: 12px;
  background: #f9fafb;
}

.mcp-section {
  margin-bottom: 16px;
}

.mcp-section:last-child {
  margin-bottom: 0;
}

.mcp-section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 8px;
  padding: 4px 0;
}

.mcp-section-title .el-icon {
  font-size: 14px;
  color: #667eea;
}

.mcp-params-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mcp-param-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 13px;
  padding: 6px 10px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.param-label {
  font-weight: 500;
  color: #6b7280;
  flex-shrink: 0;
}

.param-value {
  color: #1f2937;
  word-break: break-all;
}

.mcp-output-formatted {
  padding: 10px 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  font-size: 13px;
  color: #1f2937;
  line-height: 1.6;
}

.mcp-output-raw {
  background: #1f2937;
  border-radius: 6px;
  padding: 12px;
  overflow-x: auto;
}

.mcp-output-raw pre {
  margin: 0;
  color: #10b981;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.expand-icon {
  transition: transform 0.3s;
  color: #9ca3af;
}

.expand-icon.rotated {
  transform: rotate(90deg);
}

.detail-content {
  font-size: 13px;
  color: #374151;
  line-height: 1.6;
  padding: 6px 0;
}

/* 简化的 ReAct 步骤 */
.react-steps-simple {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.step-item {
  padding: 8px 10px;
  background: white;
  border-radius: 6px;
  border-left: 3px solid #667eea;
  transition: all 0.2s;
}

.step-item:hover {
  background: #fafbff;
  border-left-color: #4f5fd7;
}

.step-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}

.step-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}

.step-thought-text {
  flex: 1;
  color: #374151;
  line-height: 1.5;
}

.step-action-badge {
  padding: 2px 8px;
  background: #e0f2fe;
  color: #0369a1;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  font-family: 'Monaco', 'Menlo', monospace;
  white-space: nowrap;
}

/* MCP 调用简化列表 */
.mcp-calls-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 6px 0;
}

.mcp-call-simple {
  padding: 4px 10px;
  background: #eff6ff;
  border: 1px solid #dbeafe;
  border-radius: 4px;
  font-size: 11px;
  color: #1e40af;
  font-weight: 500;
}

.mcp-tool-name {
  font-family: 'Monaco', 'Menlo', monospace;
}

.expert-status-badge {
  padding: 4px 10px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 任务描述样式 */
.expert-task {
  margin: 8px 0;
  padding: 10px 12px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 8px;
  border-left: 3px solid #0ea5e9;
}

.task-label {
  font-size: 11px;
  font-weight: 600;
  color: #0369a1;
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.task-content {
  font-size: 13px;
  color: #0c4a6e;
  line-height: 1.5;
}

/* 完成汇报样式 */
.expert-completion {
  margin: 8px 0;
  padding: 10px 12px;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  border-radius: 8px;
  border-left: 3px solid #22c55e;
}

.completion-label {
  font-size: 11px;
  font-weight: 600;
  color: #15803d;
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.completion-content {
  font-size: 13px;
  color: #14532d;
  line-height: 1.5;
  font-weight: 500;
}

/* ReAct 思考过程样式 */
.expert-react-section {
  margin: 12px 0;
  border: 1px solid #e0e7ff;
  border-radius: 8px;
  overflow: hidden;
  background: #fafaff;
}

.react-header {
  padding: 10px 12px;
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background 0.2s;
}

.react-header:hover {
  background: linear-gradient(135deg, #e0e7ff 0%, #ddd6fe 100%);
}

.react-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #4338ca;
}

.react-header-left .el-icon {
  font-size: 16px;
}

.react-expand-icon {
  transition: transform 0.3s;
  color: #6366f1;
}

.react-expand-icon.rotated {
  transform: rotate(90deg);
}

.react-steps {
  padding: 8px;
  background: white;
}

.react-step {
  margin: 8px 0;
  padding: 10px;
  background: #f9fafb;
  border-radius: 6px;
  border-left: 3px solid #818cf8;
}

.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  cursor: pointer;
  margin-bottom: 8px;
}

.step-number {
  font-size: 12px;
  font-weight: 700;
  color: #4338ca;
  background: white;
  padding: 2px 8px;
  border-radius: 4px;
}

.step-expand-icon {
  transition: transform 0.3s;
  color: #6366f1;
  font-size: 14px;
}

.step-expand-icon.rotated {
  transform: rotate(90deg);
}

.step-thought,
.step-action,
.step-observation,
.step-reflection {
  margin: 6px 0;
  padding: 6px 8px;
  font-size: 12px;
  line-height: 1.6;
}

.step-thought {
  background: white;
  border-radius: 4px;
}

.step-label {
  font-weight: 600;
  color: #374151;
  margin-right: 6px;
  display: inline-block;
}

.step-content {
  color: #1f2937;
}

.step-details {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #e5e7eb;
}

.step-action {
  background: #f0f9ff;
  border-radius: 4px;
}

.step-observation {
  background: #fefce8;
  border-radius: 4px;
}

.observation-content {
  font-family: 'Courier New', monospace;
  font-size: 11px;
  color: #713f12;
  white-space: pre-wrap;
  word-break: break-all;
}

.step-reflection {
  background: #f5f3ff;
  border-radius: 4px;
}

.step-params {
  margin: 4px 0;
  padding: 6px 8px;
  background: #1f2937;
  color: #10b981;
  font-size: 11px;
  border-radius: 4px;
  overflow-x: auto;
}

.expert-status-badge.pending {
  background: #f5f7fa;
  color: #9ca3af;
  box-shadow: none;
}

.expert-status-badge.running {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  animation: pulse-badge 1.5s ease-in-out infinite;
}

.expert-status-badge.completed {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  color: white;
}

.expert-status-badge.error {
  background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
  color: white;
}

@keyframes pulse-badge {
  0%, 100% { 
    opacity: 1; 
    transform: scale(1);
  }
  50% { 
    opacity: 0.85; 
    transform: scale(1.02);
  }
}

/* 专家结果详细展示 */
.expert-result-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

/* 数据记录专家结果样式 - 极简风格 */
.data-record-result {
  margin-bottom: 10px;
  padding: 14px 16px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #e8eaed;
  transition: all 0.2s ease;
}

.data-record-result:hover {
  background: #f5f7fa;
  border-color: #d1d5db;
}

.record-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 12px;
  font-size: 14px;
}

.record-header .el-icon {
  font-size: 16px;
  color: #10b981;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.record-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: white;
  border-radius: 6px;
  font-size: 13px;
  border: 1px solid #f0f0f0;
  transition: all 0.2s ease;
}

.record-item:hover {
  border-color: #e0e0e0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.record-type {
  font-weight: 600;
  color: #6b7280;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.record-value {
  color: #111827;
  font-weight: 500;
  font-size: 14px;
}

/* 问诊专家评估结果样式 - 极简风格 */
.consultation-assessment {
  margin-bottom: 10px;
  padding: 14px 16px;
  background: #fefcf9;
  border-radius: 8px;
  border: 1px solid #f59e0b;
  border-left-width: 3px;
  transition: all 0.2s ease;
}

.consultation-assessment:hover {
  background: #fef3e7;
  box-shadow: 0 1px 3px rgba(245, 158, 11, 0.1);
}

.assessment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #92400e;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
}

.assessment-header .el-icon {
  font-size: 16px;
  color: #f59e0b;
}

.assessment-questions {
  font-size: 13px;
  color: #374151;
}

.questions-label {
  color: #6b7280;
  font-weight: 600;
  margin-bottom: 8px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.questions-list {
  margin: 8px 0;
  padding-left: 20px;
  color: #111827;
}

.questions-list li {
  margin: 6px 0;
  line-height: 1.6;
  position: relative;
}

.questions-list li::marker {
  color: #f59e0b;
}

.assessment-reason {
  margin-top: 12px;
  padding: 10px 12px;
  background: white;
  border-radius: 6px;
  font-size: 12px;
  color: #6b7280;
  border-left: 2px solid #fbbf24;
  font-style: normal;
  line-height: 1.5;
}

/* MCP工具调用详细样式 */
.expert-mcp-calls {
  margin-bottom: 10px;
}

.mcp-call-item {
  margin-bottom: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 10px;
  border-left: 4px solid #667eea;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.mcp-call-item:hover {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateX(2px);
}

.mcp-call-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #667eea;
  font-size: 14px;
  font-weight: 600;
  padding: 4px 6px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.mcp-call-header:hover {
  background: rgba(64, 158, 255, 0.1);
}

.mcp-header-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.mcp-expand-icon {
  display: flex;
  align-items: center;
  transition: transform 0.3s ease;
  color: #409eff;
}

.mcp-expand-icon.rotated {
  transform: rotate(90deg);
}

.mcp-call-details {
  margin-top: 8px;
  padding: 8px;
  background: #fafafa;
  border-radius: 4px;
}

.mcp-view-toggle {
  margin-bottom: 8px;
  text-align: right;
}

.mcp-io-section {
  margin-bottom: 14px;
}

.mcp-io-label {
  font-size: 11px;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 8px;
  padding: 6px 10px;
  background: #fafbfc;
  border-left: 2px solid #667eea;
  border-radius: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 格式化视图样式 */
.mcp-formatted-view {
  animation: fadeIn 0.3s ease-in;
}

.mcp-formatted-content {
  background: white;
  border-radius: 6px;
  padding: 10px 14px;
  border: 1px solid #f0f0f0;
}

.formatted-item {
  display: flex;
  align-items: baseline;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 13px;
  gap: 12px;
}

.formatted-item:last-child {
  border-bottom: none;
}

.item-icon {
  margin-right: 6px;
  font-size: 16px;
}

.item-label {
  font-weight: 600;
  color: #9ca3af;
  font-size: 12px;
  min-width: 90px;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  flex-shrink: 0;
}

.item-value {
  color: #111827;
  flex: 1;
  word-break: break-word;
  font-weight: 500;
}

.item-details {
  color: #909399;
  font-size: 12px;
  margin-left: 4px;
}

/* 原始日志视图样式 */
.mcp-raw-view {
  animation: fadeIn 0.3s ease-in;
}

.mcp-io-content {
  background: white;
  border-radius: 4px;
  padding: 8px;
  overflow-x: auto;
  border: 1px solid #e4e7ed;
}

.mcp-io-content pre {
  margin: 0;
  font-family: 'Courier New', Courier, monospace;
  font-size: 11px;
  color: #303133;
  white-space: pre-wrap;
  word-wrap: break-word;
  line-height: 1.5;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* MCP工具调用样式（旧版兼容） */
.expert-mcp-tool {
  margin-bottom: 8px;
  padding: 8px;
  background: linear-gradient(135deg, #e8f4fd 0%, #d4e9f7 100%);
  border-radius: 6px;
  border-left: 3px solid #409eff;
}

.mcp-tool-header {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #409eff;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 6px;
}

.mcp-tool-data {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mcp-data-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
}

.mcp-data-item .data-label {
  color: #909399;
  font-weight: 500;
}

.mcp-data-item .data-value {
  color: #409eff;
  font-weight: 600;
}

/* 专家分析内容样式 */
.expert-analysis-content {
  background: white;
  border-radius: 6px;
  overflow: hidden;
}

.analysis-toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eaed 100%);
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid #67c23a;
}

.analysis-toggle:hover {
  background: linear-gradient(135deg, #e8eaed 0%, #dfe1e4 100%);
}

.toggle-label {
  font-size: 12px;
  font-weight: 600;
  color: #606266;
}

.analysis-detail {
  padding: 12px;
  background: white;
  border-top: 1px solid #e4e7ed;
  animation: slideDown 0.3s ease-out;
}

.analysis-markdown {
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
}

.analysis-preview {
  padding: 8px 12px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  background: #fafbfc;
}

.expert-result-preview {
  margin-top: 8px;
  padding: 8px;
  background: white;
  border-radius: 4px;
  border-left: 3px solid #67c23a;
}

.result-content {
  font-size: 12px;
  color: #606266;
  line-height: 1.5;
}

.expert-error {
  margin-top: 8px;
  padding: 8px;
  background: white;
  border-radius: 4px;
  border-left: 3px solid #f56c6c;
  color: #f56c6c;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* AI思考过程样式 (豆包风格) */
.thinking-content {
  margin: 4px 0 8px 0;
}

.thinking-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 0;
  transition: opacity 0.2s;
}

.thinking-toggle.clickable {
  cursor: pointer;
}

.thinking-toggle.clickable:hover {
  opacity: 0.8;
}

.thinking-dots {
  color: #c0c4cc;
  font-size: 12px;
  line-height: 1;
  animation: thinking-pulse 1.5s ease-in-out infinite;
}

@keyframes thinking-pulse {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}

.thinking-label {
  font-size: 12px;
  color: #909399;
  font-weight: 400;
}

.thinking-preview {
  font-size: 12px;
  color: #c0c4cc;
  font-style: italic;
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.thinking-details {
  margin: 6px 0;
  padding: 8px 12px;
  font-size: 12px;
  line-height: 1.4;
  color: #909399;
  background: rgba(240, 242, 245, 0.3);
  border-radius: 4px;
  white-space: pre-wrap;
  border-left: 2px solid #e4e7ed;
}

.message-text {
  line-height: 1.6;
  word-break: break-word;
}

/* Markdown内容样式 */
.message-text h1, .message-text h2, .message-text h3, 
.message-text h4, .message-text h5, .message-text h6 {
  margin: 16px 0 8px 0;
  font-weight: 600;
  line-height: 1.3;
}

.message-text h1 { font-size: 1.5em; }
.message-text h2 { font-size: 1.3em; }
.message-text h3 { font-size: 1.1em; }

.message-text p {
  margin: 8px 0;
}

.message-text ul, .message-text ol {
  margin: 8px 0;
  padding-left: 20px;
}

.message-text li {
  margin: 4px 0;
  line-height: 1.5;
}

.message-text blockquote {
  margin: 12px 0;
  padding: 8px 16px;
  border-left: 4px solid #ddd;
  background: #f9f9f9;
  color: #666;
  font-style: italic;
}

.message-text code {
  background: #f1f3f4;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
  color: #e91e63;
}

.message-text pre {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 12px 0;
  border: 1px solid #e9ecef;
}

.message-text pre code {
  background: none;
  padding: 0;
  color: #333;
  font-size: 0.9em;
}

.message-text a {
  color: #409EFF;
  text-decoration: none;
}

.message-text a:hover {
  text-decoration: underline;
}

.message-text table {
  border-collapse: collapse;
  margin: 12px 0;
  width: 100%;
}

.message-text th, .message-text td {
  border: 1px solid #ddd;
  padding: 8px 12px;
  text-align: left;
}

.message-text th {
  background: #f5f5f5;
  font-weight: 600;
}

.message-text strong {
  font-weight: 600;
  color: #303133;
}

.message-text em {
  font-style: italic;
  color: #606266;
}

.message-time {
  font-size: 12px;
  margin-top: 8px;
  opacity: 0.7;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409EFF;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.chat-input-area {
  background: white;
  border-top: 1px solid #e4e7ed;
  padding: 20px;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.empty-disclaimer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 12px;
  color: #9ca3af;
  font-size: 13px;
  animation: fadeIn 0.5s ease-out 0.2s both;
}

.empty-disclaimer .el-icon {
  font-size: 14px;
  opacity: 0.8;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 输入框包装器 */
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  border-radius: 18px;
  border: 1.5px solid #e8eaed;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 12px 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.input-wrapper:hover {
  border-color: #d1d5db;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
}

.input-wrapper:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.08);
}

/* 输入框左侧按钮 */
.input-left-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.input-left-actions .el-button {
  color: #909399;
  font-size: 18px;
}

.input-left-actions .el-button:hover {
  color: #667eea;
}

/* 输入框右侧按钮 */
.input-right-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 8px;
}

.depth-think-btn {
  color: #667eea !important;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 8px;
  background: rgba(102, 126, 234, 0.05);
}

.depth-think-btn:hover {
  background: rgba(102, 126, 234, 0.1);
}

/* 空状态下的输入框样式 */
.empty-input-section .input-wrapper {
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08);
  border-radius: 28px;
  padding: 14px 18px;
  border: 1.5px solid #e8eaed;
  background: white;
  animation: fadeIn 0.5s ease-out 0.3s both;
}

.empty-input-section .input-wrapper:hover {
  border-color: #d1d5db;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.empty-input-section .input-wrapper:focus-within {
  border-color: #667eea;
  box-shadow: 0 4px 24px rgba(102, 126, 234, 0.2);
}

.empty-input-section .message-input :deep(.el-textarea__inner) {
  font-size: 15px;
  line-height: 24px;
}

/* 功能标签 */
.feature-tags {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 16px;
  animation: fadeIn 0.5s ease-out 0.4s both;
}

.feature-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.feature-tag .el-icon {
  font-size: 16px;
  color: #909399;
}

.feature-tag:hover {
  background: white;
  border-color: #667eea;
  color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.feature-tag:hover .el-icon {
  color: #667eea;
}

/* 输入框样式重置 */
.message-input {
  flex: 1;
}

.message-input :deep(.el-textarea__inner) {
  border: none;
  padding: 8px 0;
  background: transparent;
  box-shadow: none;
  resize: none;
  font-size: 15px;
  line-height: 24px;
  color: #303133;
  min-height: 24px !important;
}

.message-input :deep(.el-textarea__inner):focus {
  box-shadow: none;
  border: none;
}

.message-input :deep(.el-textarea__inner)::placeholder {
  color: #a8abb2;
}

/* 发送按钮 */
.input-send-btn {
  display: flex;
  align-items: center;
}

.input-send-btn .el-button {
  width: 38px;
  height: 38px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.input-send-btn .el-button:hover {
  background: linear-gradient(135deg, #5568d3 0%, #63408a 100%);
  transform: translateY(-1px) scale(1.05);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.input-send-btn .el-button:active {
  transform: translateY(0) scale(0.98);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.input-send-btn .el-button .el-icon {
  font-size: 18px;
  color: white;
}

/* 发送按钮淡入淡出动画 */
.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-scale-enter-from {
  opacity: 0;
  transform: scale(0.8);
}

.fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

/* 底部操作栏 */
.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding: 0 4px;
}

.char-count {
  font-size: 12px;
  color: #909399;
}

.footer-actions {
  display: flex;
  gap: 8px;
}

.footer-actions .el-button {
  font-size: 13px;
  color: #606266;
  padding: 4px 8px;
}

.footer-actions .el-button:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

/* 输入容器 */
.input-container {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
}


/* 工具调用结果样式 */
.tool-calls-info {
  margin-top: 12px;
  padding: 12px;
  background: #f0f9ff;
  border: 1px solid #e1f5fe;
  border-radius: 6px;
  font-size: 13px;
}

.tool-calls-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-weight: 500;
  color: #0277bd;
}

.tool-call-item {
  margin-bottom: 6px;
  padding: 6px 8px;
  background: white;
  border-radius: 4px;
  border-left: 3px solid #4fc3f7;
}

.tool-name {
  font-weight: 500;
  color: #01579b;
  margin-bottom: 4px;
}

.tool-result {
  color: #37474f;
}

/* 健康记录添加结果组件样式 */
.health-record-add-result {
  cursor: pointer;
  border: 1px solid #e1f5fe;
  border-radius: 8px;
  background: #ffffff;
  transition: all 0.3s ease;
  overflow: hidden;
}

.health-record-add-result:hover {
  border-color: #4fc3f7;
  box-shadow: 0 2px 8px rgba(79, 195, 247, 0.2);
}

.result-header {
  display: flex;
  align-items: center;
  padding: 12px;
  gap: 12px;
}

.result-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4fc3f7, #29b6f6);
  color: white;
  font-size: 16px;
  flex-shrink: 0;
}

.result-info {
  flex: 1;
  min-width: 0;
}

.result-title {
  font-weight: 500;
  color: #01579b;
  margin-bottom: 2px;
  font-size: 14px;
}

.result-value {
  color: #37474f;
  font-size: 13px;
  opacity: 0.8;
}

.expand-icon {
  display: flex;
  align-items: center;
  color: #90a4ae;
  transition: transform 0.3s ease;
}

.expand-icon.rotated {
  transform: rotate(90deg);
}

.result-details {
  padding: 0 12px 12px 12px;
  border-top: 1px solid #f5f5f5;
  background: #fafafa;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    max-height: 0;
  }
  to {
    opacity: 1;
    max-height: 300px;
  }
}

.detail-item {
  display: flex;
  align-items: flex-start;
  margin: 8px 0;
  font-size: 13px;
}

.detail-item .label {
  font-weight: 500;
  color: #546e7a;
  min-width: 80px;
  margin-right: 8px;
}

.detail-item .value {
  color: #37474f;
  flex: 1;
}

.record-details pre {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  padding: 8px;
  margin: 4px 0 0 0;
  font-size: 12px;
  color: #495057;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.health-records .record-type {
  margin: 2px 0;
}

.usage-info {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #e0e0e0;
  color: #999;
  font-size: 11px;
}

.error-message {
  background: #fef2f2;
  border-color: #fecaca;
}

.error-message .message-text {
  color: #dc2626;
}

/* 流式状态显示样式 */
.message-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
  border: 1px solid #b3e5fc;
  border-radius: 6px;
  color: #0277bd;
  font-size: 13px;
  font-weight: 500;
}

.message-status .el-icon {
  color: #29b6f6;
}

/* 简化的消息状态样式 */
.message-item.streaming .message-bubble {
  border-left: 3px solid #29b6f6;
  animation: pulse 2s infinite;
}

.message-item.completed .message-bubble {
  border-left: none;
}

@keyframes pulse {
  0% { opacity: 0.8; }
  50% { opacity: 1; }
  100% { opacity: 0.8; }
}

/* ================== 交互式工具组件样式 ================== */

.interactive-tool-result {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  margin-top: 8px;
  background: white;
  transition: all 0.3s ease;
}

.interactive-tool-result:hover {
  border-color: #c0c4cc;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.result-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  background: #fafbfc;
  border-bottom: 1px solid #e4e7ed;
  transition: background-color 0.2s;
}

.result-header:hover {
  background: #f0f2f5;
}

.result-icon {
  font-size: 20px;
  margin-right: 12px;
  flex-shrink: 0;
}

.result-info {
  flex: 1;
}

.result-title {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  margin-bottom: 2px;
}

.result-summary {
  color: #606266;
  font-size: 12px;
}

.expand-icon {
  margin-left: 8px;
  transition: transform 0.3s ease;
  color: #909399;
}

.expand-icon.rotated {
  transform: rotate(90deg);
}

.result-details {
  padding: 16px;
  background: white;
  border-top: 1px solid #f0f2f5;
}

/* 健康记录网格样式 */
.records-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.record-type-card {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  border-left: 4px solid #409eff;
}

.record-type-card h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 14px;
}

.record-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.record-stats .count {
  font-weight: 600;
  color: #409eff;
}

.record-stats .latest {
  font-size: 12px;
  color: #909399;
}

.latest-value {
  font-size: 16px;
  font-weight: 600;
  color: #67c23a;
}

/* 医生卡片样式 */
.doctors-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 12px;
}

.doctor-card {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  transition: all 0.2s;
}

.doctor-card:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.doctor-card.recommended {
  border-left: 4px solid #67c23a;
}

.doctor-info h4 {
  margin: 0 0 4px 0;
  color: #303133;
}

.doctor-info .department {
  color: #409eff;
  font-size: 12px;
  margin: 2px 0;
}

.doctor-info .title {
  color: #606266;
  font-size: 12px;
  margin: 2px 0;
}

.doctor-info .specialization {
  color: #909399;
  font-size: 11px;
  margin: 4px 0;
}

.doctor-status {
  margin-top: 8px;
}

.status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

.status.online {
  background: #e8f5e8;
  color: #67c23a;
}

.status.offline {
  background: #fef0f0;
  color: #f56c6c;
}

.match-score {
  margin-top: 8px;
  text-align: right;
}

.match-score .score {
  background: #e8f5e8;
  color: #67c23a;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

/* 咨询记录样式 */
.consultations-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.consultation-item {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  border-left: 4px solid #e6a23c;
  margin-bottom: 8px;
}

.consultation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.consultation-title {
  font-weight: 600;
  color: #303133;
}

.consultation-date {
  font-size: 12px;
  color: #909399;
}

.consultation-status .status {
  background: #e8f4fd;
  color: #409eff;
}

/* 科室网格样式 */
.departments-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.department-card {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  border-left: 4px solid #909399;
}

.department-card h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.department-stats {
  font-size: 12px;
  color: #606266;
}

/* 系统概览网格样式 */
.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.overview-item {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.overview-item h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
}

.overview-item p {
  margin: 4px 0;
  color: #606266;
  font-size: 14px;
}

/* 详情内容样式 */
.detail-content {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.detail-header h4 {
  margin: 0;
  color: #303133;
}

.detail-date {
  font-size: 12px;
  color: #909399;
}

.detail-body p {
  color: #606266;
  line-height: 1.5;
  margin-bottom: 12px;
}

/* 通用样式 */
.no-data {
  text-align: center;
  color: #909399;
  padding: 24px;
  font-style: italic;
}

.detail-item {
  margin: 8px 0;
  display: flex;
  align-items: flex-start;
}

.detail-item .label {
  font-weight: 600;
  color: #606266;
  min-width: 80px;
  margin-right: 8px;
}

.detail-item .value {
  color: #303133;
  flex: 1;
}

.record-details pre {
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  overflow-x: auto;
  white-space: pre-wrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .empty-state {
    padding: 40px 16px;
  }
  
  .empty-content {
    max-width: 100%;
  }
  
  .brand-header {
    margin-bottom: 36px;
  }
  
  .brand-title {
    font-size: 26px;
  }
  
  .feature-tags {
    gap: 10px;
  }
  
  .feature-tag {
    font-size: 12px;
    padding: 6px 12px;
  }
  
  .feature-tag .el-icon {
    font-size: 14px;
  }
  
  .chat-container {
    padding: 16px;
  }
  
  .chat-input-area {
    padding: 16px;
  }
  
  .empty-input-section .input-wrapper {
    padding: 12px 48px 12px 16px;
    border-radius: 20px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .input-wrapper {
    padding: 10px 48px 10px 12px;
    border-radius: 12px;
  }
  
  .input-send-btn .el-button {
    width: 32px;
    height: 32px;
  }
  
  .footer-actions {
    flex-wrap: wrap;
  }
  
  /* 交互式组件移动端适配 */
  .interactive-tool-result {
    margin: 8px 0;
  }
  
  .result-header {
    padding: 10px 12px;
  }
  
  .result-icon {
    font-size: 18px;
    margin-right: 8px;
  }
  
  .result-title {
    font-size: 13px;
  }
  
  .result-summary {
    font-size: 11px;
  }
  
  .result-details {
    padding: 12px;
  }
  
  .records-grid,
  .doctors-grid,
  .departments-grid,
  .overview-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .doctor-card,
  .record-type-card,
  .department-card,
  .overview-item {
    padding: 8px;
  }
  
  .consultation-item {
    padding: 8px;
  }
  
  .consultation-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style> 