<template>
  <div class="create-announcement">
    <h1>发布公告</h1>
    <form @submit.prevent="submitAnnouncement">
      <div class="form-group">
        <label for="title">公告标题</label>
        <input v-model="announcement.title" type="text" id="title" placeholder="请输入公告标题" required />
      </div>
      <div class="form-group">
        <label for="content">公告内容</label>
        <textarea v-model="announcement.content" id="content" placeholder="请输入公告内容" required rows="10"></textarea>
      </div>
      <div class="form-group">
        <label for="coverImage">上传封面</label>
        <input type="file" id="coverImage" @change="handleFileUpload"/>
      </div>
      <button type="submit">发布公告</button>
    </form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      announcement: {
        title: '',
        content: ''
      }
    };
  },
  methods: {
    submitAnnouncement() {
      console.log('公告已提交:', this.announcement);
    },
    handleFileUpload(event) {
      const file = event.target.files[0];
      if (file) {
        console.log('上传的封面文件:', file.name);
      }
    }
  }
}
</script>

<style scoped>
.create-announcement {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
}

input, textarea {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

textarea {
  resize: vertical;
}

button {
  padding: 0.8rem 2rem;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #40a9ff;
}
</style>
