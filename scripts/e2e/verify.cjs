const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const root = path.resolve(__dirname, '../..');
const web = process.env.DEMO_WEB_URL || 'http://localhost:5173';
const api = process.env.DEMO_API_URL || 'http://localhost:8080';
for (const u of [web,api]) assert(['localhost','127.0.0.1'].includes(new URL(u).hostname), 'This test is restricted to a local fictional demo.');
const results = { date: new Date().toISOString(), environment: 'Local fictional demo only', checks: [], browserErrors: [], httpFailures: [], fixtures: {} };
const stamp = Date.now().toString();
const screenshots = path.join(root, 'docs/screenshots'); fs.mkdirSync(screenshots,{recursive:true});
async function request(route, method='GET', body) {
 const r = await fetch(api+route,{method,headers:body?{'Content-Type':'application/json'}:{},body:body?JSON.stringify(body):undefined});
 const text=await r.text();let data;try{data=JSON.parse(text)}catch{data=text}return {status:r.status,data};
}
async function until(fn, timeout=12000){const t=Date.now();while(Date.now()-t<timeout){if(await fn())return;await new Promise(r=>setTimeout(r,250));}throw Error('Condition did not become true within '+timeout+'ms');}
async function record(name, fn){try{const detail=await fn();results.checks.push({name,status:'PASS',detail:detail||null});console.log('PASS '+name);}catch(e){results.checks.push({name,status:'FAIL',detail:String(e.message).split('\n')[0]});console.log('FAIL '+name+': '+String(e.message).split('\n')[0]);}}
async function login(page,role,email){await page.goto(web+'/login');await page.locator('#email').fill(email);await page.locator('#password').fill('DemoOnly-2026!');await page.locator('#role').selectOption(role);await page.locator('button[type=submit]').click();await page.waitForURL('**/'+role+'-home');assert.equal(await page.evaluate(()=>localStorage.getItem('role')),role);}
function observe(page){page.on('pageerror',e=>results.browserErrors.push(e.message.split('\n')[0]));page.on('response',r=>{if(r.status()>=400 && r.url().startsWith(api))results.httpFailures.push({path:new URL(r.url()).pathname,status:r.status()});});page.on('dialog',d=>d.accept());}
(async()=>{
 const browser=await chromium.launch({channel:process.env.BROWSER_CHANNEL||'msedge',headless:true});
 const contexts=await Promise.all([1,2,3].map(()=>browser.newContext({viewport:{width:1440,height:1000},locale:'zh-CN',timezoneId:'Asia/Shanghai'})));
 for (const context of contexts) await context.route('**/*',route=>{const u=new URL(route.request().url());return ['localhost','127.0.0.1'].includes(u.hostname)?route.continue():route.abort();});
 const [student,enterprise,studentB]=await Promise.all(contexts.map(c=>c.newPage()));[student,enterprise,studentB].forEach(observe);
 try{
 await record('Browser login: student and enterprise',async()=>{await login(student,'student','student.a@example.invalid');await login(enterprise,'enterprise','enterprise@example.invalid');await login(studentB,'student','student.b@example.invalid');});
 await record('Password fields absent from login JSON',async()=>{const r=await request('/api/login/student','POST',{email:'student.a@example.invalid',password:'DemoOnly-2026!'});assert.equal(r.status,200);assert(!('password' in r.data));});
 await record('Invalid login rejected',async()=>{const r=await request('/api/login/student','POST',{email:'student.a@example.invalid',password:'intentionally-wrong-demo-password'});assert.equal(r.status,400);});
 await record('Browser messaging: HTTP send, receiver polling, reload persistence',async()=>{
  await student.goto(web+'/student/chat/2001');await enterprise.goto(web+'/chat/1001');
  const a='Portfolio demo: student message '+stamp,b='Portfolio demo: enterprise reply '+stamp;
  await student.getByPlaceholder('输入消息...').fill(a);await student.locator('.send-button').click();
  await enterprise.getByText(a,{exact:true}).waitFor();
  await enterprise.getByPlaceholder('输入消息...').fill(b);await enterprise.locator('.send-button').click();await student.getByText(b,{exact:true}).waitFor();
  await student.reload();await student.getByText(a,{exact:true}).waitFor();await student.getByText(b,{exact:true}).waitFor();
  const history=await request('/api/chat/history/1001/2001');assert.equal(history.status,200);assert(history.data.some(m=>m.content===a));assert(history.data.some(m=>m.content===b));
  results.fixtures.messages=[a,b];await student.screenshot({path:path.join(screenshots,'messaging.png'),fullPage:true});return 'Two separate browser contexts; no network responses mocked.';
 });
 await record('Unread count changes and mark-read persistence',async()=>{
  await student.goto(web+'/forum');await enterprise.goto(web+'/forum');
  await request('/api/chat/mark-read/1001/2001','POST');const before=(await request('/api/chat/unread/1001')).data.unreadCount;
  const r=await request('/api/chat/send','POST',{senderId:'2001',senderName:'Example Collaboration Lab',senderType:'ENTERPRISE',receiverId:'1001',content:'Unread verification '+stamp});assert.equal(r.status,200);
  assert.equal((await request('/api/chat/unread/1001')).data.unreadCount,before+1);
  await student.goto(web+'/student/chat/2001');await until(async()=> (await request('/api/chat/unread/1001')).data.unreadCount===0);
  const saved=(await request('/api/chat/history/1001/2001')).data.find(x=>x.id===r.data.id);assert.equal(saved.read,true);
  await student.goto(web+'/forum');
 });
 await record('Browser forum: create post, comment, reply, like/unlike and reload',async()=>{
  await student.goto(web+'/forum/create');await student.locator('#title').fill('Backend collaboration discussion '+stamp);await student.locator('#category').selectOption('COMMUNITY');await student.locator('#subcategory').selectOption('QA');await student.locator('#content').fill('Fictional portfolio demonstration: discussing message persistence and project collaboration.');
  const created=student.waitForResponse(r=>r.url()===api+'/api/forum/posts'&&r.request().method()==='POST');await student.locator('.submit-btn').click();const postResponse=await created;assert.equal(postResponse.status(),200);const post=await postResponse.json();results.fixtures.postId=post.postId;
  await student.goto(web+'/forum/posts/'+post.postId);await student.getByPlaceholder('写下你的评论...').fill('How is a project application reviewed?');await student.locator('.submit-comment-btn').click();await student.locator('.comment-content').getByText('How is a project application reviewed?',{exact:true}).waitFor();
  await enterprise.goto(web+'/forum/posts/'+post.postId);await enterprise.locator('.comment-card .reply-btn').first().click();await enterprise.locator('.reply-form textarea').fill('The enterprise records approval or rejection with feedback.');await enterprise.locator('.reply-form .submit-btn').click();await enterprise.locator('.child-comment').getByText('The enterprise records approval or rejection with feedback.',{exact:true}).waitFor();
  // Wait for each toggle response before reading detail: detail GET itself updates viewCount.
  // Overlapping that write with a like can expose the legacy lost-update bug.
  for (const expected of [1,0,1]) {
   const pendingLike=enterprise.waitForResponse(r=>r.url().includes('/api/forum/posts/'+post.postId+'/like')&&r.request().method()==='POST');
   await enterprise.locator('.likes').click();const response=await pendingLike;assert.equal(response.status(),200);assert.equal((await response.json()).likeCount,expected);
  }
  await enterprise.reload();await enterprise.locator('.likes.liked').waitFor();await enterprise.locator('.child-comment').getByText('The enterprise records approval or rejection with feedback.',{exact:true}).waitFor();await enterprise.screenshot({path:path.join(screenshots,'forum.png'),fullPage:true});
 });
 await record('Browser application: submit, reject, resubmit, approve, reload',async()=>{
  const newProject=await request('/api/projects','POST',{enterpriseId:2001,title:'Portfolio Review Workflow '+stamp,description:'Fictional application review exercise.',projectDirection:'Backend Development',requiredSkills:'Java, SQL',maxParticipants:2,startDate:'2026-10-01',endDate:'2027-06-30',collaborationType:'产学研合作',location:'Demo City'});assert.equal(newProject.status,200);const pid=newProject.data.projectId;results.fixtures.projectId=pid;
  async function apply(){await studentB.goto(web+'/projects/'+pid+'/apply');await studentB.locator('#applicationReason').fill('I would like to practice backend API development with this fictional project.');await studentB.locator('#contactInfo').fill('student.b@example.invalid');const pending=studentB.waitForResponse(r=>r.url().includes('/api/projects/'+pid+'/apply')&&r.request().method()==='POST');await studentB.locator('.submit-btn').click();const response=await pending;assert.equal(response.status(),200);return response.json();}
  const app=await apply();assert.equal(app.status,'PENDING');results.fixtures.applicationId=app.applicationId;
  const duplicate=await request('/api/projects/'+pid+'/apply?studentId=1002','POST',{applicationReason:'Duplicate fictional request'});assert(duplicate.status>=400);
  await enterprise.goto(web+'/enterprise/projects/applications/'+app.applicationId);await enterprise.locator('#feedback').fill('Please describe your experience with Java.');await enterprise.locator('.reject-btn').click();await until(async()=> (await request('/api/projects/applications/'+app.applicationId)).data.status==='REJECTED');
  const again=await apply();assert.equal(again.applicationId,app.applicationId);assert.equal(again.status,'PENDING');
  await enterprise.goto(web+'/enterprise/projects/applications/'+app.applicationId);await enterprise.locator('#feedback').fill('Approved for the fictional collaboration demonstration.');await enterprise.locator('.approve-btn').click();await until(async()=> (await request('/api/projects/applications/'+app.applicationId)).data.status==='APPROVED');
  await enterprise.reload();await enterprise.locator('.review-result').waitFor();assert((await request('/api/projects/applications/'+app.applicationId)).data.reviewTime);await enterprise.screenshot({path:path.join(screenshots,'application-review.png'),fullPage:true});
 });
 await record('API fictional file upload and download',async()=>{
  const content='Fictional portfolio file verification '+stamp;
  const form=new FormData();form.append('file',new Blob([content],{type:'text/plain'}),'portfolio-demo.txt');
  const upload=await fetch(api+'/api/files/upload?submissionId=1',{method:'POST',body:form});assert.equal(upload.status,200);
  const data=await upload.json();const download=await fetch(api+data.fileDownloadUri);assert.equal(download.status,200);assert.equal(await download.text(),content);
 });
 await record('Development maintenance endpoint disabled',async()=>{assert.equal((await request('/api/db-fix/check-chat-message-table')).status,404);});
 }finally{await browser.close();fs.writeFileSync(path.join(root,'docs/browser-validation.json'),JSON.stringify(results,null,2)+'\n');}
 if(results.checks.some(c=>c.status==='FAIL'))process.exitCode=1;
})().catch(e=>{console.error(String(e));process.exitCode=1;});
