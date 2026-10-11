<template>
  <main class="login-page">
    <section class="login-story" aria-labelledby="platform-heading">
      <div class="platform-brand"><span class="brand-mark" aria-hidden="true">s<span>e</span></span><span>学生企业合作平台<small>STUDENT & ENTERPRISE</small></span></div>
      <div class="story-body">
        <p class="eyebrow"><span></span> CONNECT. COLLABORATE. GROW.</p>
        <h1 id="platform-heading">让想法，<br>在合作中<span class="serif-accent">发生。</span></h1>
        <p class="story-description">从一次交流，到一个共同完成的项目。<br>在这里连接彼此，把所学带入实践。</p>
        <div class="collaboration-art" aria-hidden="true">
          <div class="orbit orbit-one"></div><div class="orbit orbit-two"></div>
          <div class="art-node student-node"><span class="node-symbol">↗</span><span>学生<small>带着想法出发</small></span></div>
          <div class="art-connector"><span>共同创造</span><div>＋</div></div>
          <div class="art-node enterprise-node"><span class="node-symbol">▦</span><span>企业<small>让机会与能力相遇</small></span></div>
          <span class="art-caption">GOOD WORK STARTS WITH A CONVERSATION.</span>
        </div>
      </div>
      <div class="story-footer"><span>01 <b>交流讨论</b></span><span>02 <b>项目协作</b></span><span>03 <b>共同成长</b></span></div>
    </section>
    <section class="login-panel" aria-labelledby="login-heading">
      <div class="panel-top">每一段合作，都从这里开始 <span>↗</span></div>
      <div class="login-box">
        <p class="form-eyebrow">YOUR NEXT CHAPTER</p>
        <h2 id="login-heading">欢迎回来</h2>
        <p class="login-intro">登录账号，继续你的交流与协作。</p>
        <form @submit.prevent="login" class="login-form">
          <div class="form-group"><label for="role">以什么身份加入？</label><select v-model="role" id="role"><option value="student">学生</option><option value="enterprise">企业</option><option value="admin">管理员</option></select></div>
          <div class="form-group"><label for="email">邮箱地址</label><input type="email" id="email" v-model="email" autocomplete="username" required placeholder="请输入你的邮箱" /></div>
          <div class="form-group"><label for="password">密码</label><input type="password" id="password" v-model="password" autocomplete="current-password" required placeholder="请输入密码" /></div>
          <div v-if="errorMessage" class="error-message" role="alert">{{ errorMessage }}</div>
          <button type="submit" class="login-button"><span>登录平台</span><span aria-hidden="true">↗</span></button>
        </form>
        <p class="register-link">还没有账号？<router-link to="/register">创建账号 <span aria-hidden="true">→</span></router-link></p>
      </div>
      <div class="panel-footer"><span>学生 · 企业 · 更多可能</span><span>一起，向前一步。</span></div>
    </section>
  </main>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      email: '',
      password: '',
      role: 'student',  // 默认角色是学生
      errorMessage: ''
    };
  },
  methods: {
    async login() {
      try {
        // 根据角色进行登录请求
        const response = await axios.post(`http://localhost:8080/api/login/${this.role}`, {
          email: this.email,
          password: this.password
        });

        // 登录成功，保存 userId 和角色到 localStorage
        const user = response.data;

        localStorage.setItem("role", this.role);  // 保存角色
        localStorage.setItem("userRole", this.role);  // 保存用户角色（为了兼容性）

        // 跳转到对应的主页
        if (this.role === 'student') {
          localStorage.setItem("userId", user.studentId);  // 保存用户ID
          this.$router.push({ name: 'student-home', params: { studentId: user.id } });
        } else if (this.role === 'enterprise') {
          localStorage.setItem("userId", user.enterpriseId);  // 保存用户ID
          this.$router.push({ name: 'enterprise-home', params: { enterpriseId: user.id } });
        } else if (this.role === 'admin') {
          localStorage.setItem("userId", user.id);  // 保存用户ID
          localStorage.setItem("adminId", user.id);  // 保存管理员ID
          this.$router.push({ name: 'admin', params: { adminId: user.id } });
        }
      } catch (error) {
        this.errorMessage = '邮箱或密码错误';
      }
    }
  }
};
</script>

<style scoped>
.login-page{min-height:100svh;display:grid;grid-template-columns:1.12fr 1fr;background:#fff;color:#203b34}
.login-story{background:#edf1e9;position:relative;padding:38px clamp(32px,5vw,88px) 28px;display:flex;flex-direction:column;overflow:hidden}
.platform-brand{display:flex;gap:13px;align-items:center;font-size:16px;font-weight:650;letter-spacing:1px}.platform-brand small{display:block;font-size:9px;letter-spacing:2px;margin-top:5px;color:#617568}.brand-mark{display:grid;grid-template-columns:1fr 1fr;align-items:center;background:#234b3d;color:#fff;border-radius:12px;width:44px;height:44px;padding:7px;font-size:25px;font-weight:500;letter-spacing:-3px}.brand-mark span{color:#bfd799;margin-top:8px}
.story-body{margin:auto 0;padding:60px 0 25px}.eyebrow{display:flex;align-items:center;gap:9px;font-size:10px;letter-spacing:2px;color:#526a5c;font-weight:600}.eyebrow>span{width:6px;height:6px;background:#577342;border-radius:50%}.story-body h1{font-size:clamp(40px,4.2vw,65px);font-weight:600;line-height:1.32;letter-spacing:-2px;margin:25px 0 20px}.serif-accent{color:#52734b}.story-description{font-size:15px;line-height:1.95;color:#64746b}
.collaboration-art{position:relative;display:flex;align-items:center;justify-content:center;height:235px;margin:12px -10px 0}.orbit{position:absolute;border:1px solid #d3ddce;border-radius:50%;width:390px;height:165px;transform:rotate(-15deg)}.orbit-two{width:315px;height:215px;transform:rotate(17deg)}.art-node{z-index:1;display:flex;gap:11px;align-items:center;background:#fff;padding:18px 20px;border:1px solid #dae3d8;border-radius:13px;box-shadow:0 9px 22px #294b3910;font-weight:650;font-size:14px;transform:translateY(-15px)}.enterprise-node{transform:translateY(20px);background:#254d3d;color:#fff;border-color:#254d3d}.art-node small{display:block;font-size:10px;font-weight:400;margin-top:5px;white-space:nowrap;opacity:.7}.node-symbol{font-size:26px;font-weight:400}.art-connector{z-index:2;text-align:center;flex-shrink:0;width:66px;color:#6a7d69;font-size:9px}.art-connector div{width:30px;height:30px;margin:8px auto;border-radius:50%;background:#dce8c9;font-size:20px;display:grid;place-items:center}.art-caption{position:absolute;bottom:4px;letter-spacing:1.5px;font-size:8px;color:#789078}.story-footer{border-top:1px solid #d6dfd0;padding-top:24px;display:flex;justify-content:space-between;gap:12px;font-size:10px;color:#798777;letter-spacing:1px}.story-footer b{font-size:12px;font-weight:500;color:#415d4d;margin-left:8px}
.login-panel{display:flex;flex-direction:column;padding:42px clamp(32px,5vw,84px) 28px}.panel-top{display:flex;justify-content:flex-end;gap:18px;font-size:11px;letter-spacing:1px;color:#849089}.panel-top span{font-size:16px;color:#315c45}.login-box{width:100%;max-width:380px;margin:auto; padding:56px 0}.form-eyebrow{font-size:10px;letter-spacing:2px;color:#829182;margin:0 0 15px}.login-box h2{font-size:32px;font-weight:600;letter-spacing:-1px;margin:0 0 12px}.login-intro{color:#7c8781;font-size:13px;margin:0 0 34px}.form-group{margin-bottom:22px}.form-group label{display:block;font-size:12px;color:#405449;font-weight:550;margin-bottom:10px}.form-group input,.form-group select{width:100%;height:49px;padding:0 15px;border:1px solid #dce3dd;background:#fafbf9;border-radius:8px;font-size:13px;color:#2b4236;transition:border-color .15s,box-shadow .15s}.form-group input::placeholder{color:#9ba49e}.form-group input:focus,.form-group select:focus{outline:none;border-color:#537659;box-shadow:0 0 0 3px #53765915;background:#fff}.login-button{width:100%;height:50px;background:#294e3e;border:1px solid #294e3e;border-radius:8px;color:white;display:flex;align-items:center;justify-content:space-between;padding:0 19px;cursor:pointer;font-size:14px;font-weight:600;margin-top:29px;transition:background .2s}.login-button:hover{background:#1b3c2e}.login-button>span:last-child{font-size:20px;font-weight:400}.register-link{font-size:12px;color:#8a958d;text-align:center;margin-top:26px}.register-link a{color:#315a41;font-weight:650;margin-left:8px;text-decoration:none}.error-message{font-size:12px;line-height:1.6;padding:10px 13px;color:#a13535;background:#fff1ef;border-radius:7px}.panel-footer{display:flex;justify-content:space-between;font-size:10px;color:#97a198;gap:12px}.login-button:focus-visible,.register-link a:focus-visible{outline:3px solid #99b87e;outline-offset:4px}
@media(min-width:1500px){.story-body{max-width:590px}.login-page{max-width:1800px;margin:auto;border-inline:1px solid #e0e6dd}}
@media(max-width:850px){.login-page{grid-template-columns:1fr 1fr}.login-story{padding:28px}.story-body h1{font-size:40px}.collaboration-art{transform:scale(.8);margin-inline:-35px}.story-footer{flex-wrap:wrap}.login-panel{padding:32px}.platform-brand{font-size:13px}.story-description{font-size:13px}}
@media(max-width:620px){.login-page{display:block}.login-story{padding:24px}.story-body{padding:26px 0 4px}.story-body h1{font-size:34px;line-height:1.3;margin:14px 0}.story-body h1 br{display:none}.story-description{font-size:12px;margin-bottom:14px}.story-description br{display:none}.eyebrow{font-size:8px;margin-top:0}.collaboration-art,.story-footer{display:none}.login-panel{padding:28px 25px 20px}.panel-top{display:none}.login-box{padding:0 0 32px;max-width:420px}.login-box h2{font-size:27px}.form-eyebrow{display:none}.login-intro{margin-bottom:26px}.panel-footer{max-width:420px;width:100%;margin:auto}.platform-brand small{font-size:8px}.brand-mark{width:36px;height:36px;font-size:21px}}
</style>
