<template>
  <div class="ai-container">
    <h1>AI助手</h1>
    <div class="chat-box">
      <div v-for="(message, index) in messages" :key="index" :class="['message', message.type]">
        <div class="avatar">
          <img :src="message.avatar" alt="Avatar" />
        </div>
        <div class="content">
          <div v-html="message.text" style="white-space: pre-wrap;"></div>
          <span class="timestamp">{{ message.timestamp }}</span>
        </div>
      </div>
    </div>
    <div class="input-area">
      <input v-model="userInput" @keyup.enter="sendMessage" placeholder="请输入您的问题..." />
      <button @click="sendMessage">发送</button>
    </div>
  </div>
</template>

<script>
export default {
  data () {
    return {
      userInput: '',
      messages: [
        {
          type: 'bot',
          avatar: 'http://127.0.0.1:9527/imagesWeb/SA1675604990128.jpg',
          text: '您好！有什么我可以帮您的吗？',
          timestamp: this.getFormattedTime()
        }
      ]
    }
  },
  methods: {
    sendMessage () {
      if (this.userInput.trim() === '') return

      // 用户消息
      this.messages.push({
        type: 'user',
        avatar: 'http://127.0.0.1:9527/imagesWeb/SA1675604212612.jpg',
        text: this.userInput,
        timestamp: this.getFormattedTime()
      })
      // 模拟AI回复
      this.generateResponse(this.userInput)

      // 清空输入框
      this.userInput = ''
      // setTimeout(() => {
      //   this.messages.push({
      //     type: 'bot',
      //     avatar: 'http://127.0.0.1:9527/imagesWeb/SA1675604990128.jpg',
      //     text: '这是AI的回复：' + this.generateResponse(),
      //     timestamp: this.getFormattedTime()
      //   })
      // }, 1000)
    },
    getFormattedTime () {
      const now = new Date()
      return now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    },
    generateResponse (content) {
      this.messages.push({
        type: 'bot',
        avatar: 'http://127.0.0.1:9527/imagesWeb/SA1675604990128.jpg',
        text: '请稍后 正在加载中😋',
        timestamp: this.getFormattedTime()
      })
      this.$post(`/cos/ai/aliTyqw`, {
        content: content
      }).then((r) => {
        this.messages.push({
          type: 'bot',
          avatar: 'http://127.0.0.1:9527/imagesWeb/SA1675604990128.jpg',
          text: r.data.msg,
          timestamp: this.getFormattedTime()
        })
      })
      // const responses = [
      //   '好的，我会帮您查找相关信息。',
      //   '请问您需要了解哪方面的内容？',
      //   '感谢您的提问，我会尽力提供帮助。'
      // ]
      // return responses[Math.floor(Math.random() * responses.length)]
    }
  }
}
</script>

<style scoped>
.ai-container {
  width: 100%;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.chat-box {
  background-color: #f4f4f4;
  border-radius: 8px;
  padding: 10px;
  height: 60vh;
  overflow-y: auto;
  margin-bottom: 20px;
}

.message {
  display: flex;
  margin-bottom: 10px;
}

.message.user {
  justify-content: flex-end;
}

.message.bot {
  justify-content: flex-start;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 10px;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.content {
  background-color: #fff;
  border-radius: 8px;
  padding: 10px;
  max-width: 70%;
}

.timestamp {
  font-size: 0.8em;
  color: #999;
  display: block;
  margin-top: 5px;
  text-align: right;
}

.input-area {
  display: flex;
}

.input-area input {
  flex-grow: 1;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  margin-right: 10px;
}

.input-area button {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.input-area button:hover {
  background-color: #0056b3;
}

.content {
  background-color: #fff;
  border-radius: 12px;
  padding: 12px 16px;
  max-width: 70%;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.message.bot .content {
  border-top-left-radius: 4px;
}

.message.user .content {
  border-top-right-radius: 4px;
}

.timestamp {
  font-size: 0.75em;
  color: #888;
  display: block;
  margin-top: 6px;
  text-align: right;
}

/*.input-area {*/
/*  display: flex;*/
/*  gap: 10px;*/
/*  padding: 10px;*/
/*  background: white;*/
/*  border-radius: 8px;*/
/*  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);*/
/*}*/

.input-area input {
  flex-grow: 1;
  padding: 12px 15px;
  border: 1px solid #e1e5e9;
  border-radius: 5px;
  outline: none;
}

.input-area input:focus {
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0,123,255,0.25);
}

.input-area button {
  padding: 10px 24px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.chat-box::-webkit-scrollbar {
  width: 6px;
}

.chat-box::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.chat-box::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

.chat-box::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
