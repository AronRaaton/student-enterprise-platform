const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const {chromium}=require(process.env.PLAYWRIGHT_MODULE||'playwright');
const root=path.resolve(__dirname,'../..'), api=process.env.DEMO_API_URL||'http://localhost:8080',web=process.env.DEMO_WEB_URL||'http://localhost:5173';
for(const u of [api,web])assert(['localhost','127.0.0.1'].includes(new URL(u).hostname));
const report={date:new Date().toISOString(),test:'Actual STOMP transport and recipient delivery without polling',sockjsInfoStatus:null,connected:false,httpMessagePersisted:false,stompMessagePersisted:false,recipientDelivered:false};
const wait=ms=>new Promise(r=>setTimeout(r,ms));
(async()=>{
 const info=await fetch(api+'/ws/info',{headers:{Origin:web}});report.sockjsInfoStatus=info.status;
 const browser=await chromium.launch({channel:process.env.BROWSER_CHANNEL||'msedge',headless:true});
 try{
  const page=await browser.newPage();await page.goto(web+'/login');
  await page.evaluate(url=>{window.socketTest={frames:[],errors:[],closed:null};const w=new WebSocket(url.replace('http:','ws:')+'/ws/websocket');window.socketTest.ws=w;w.onopen=()=>w.send('CONNECT\naccept-version:1.2\nheart-beat:0,0\n\n\0');w.onmessage=e=>window.socketTest.frames.push(e.data);w.onerror=()=>window.socketTest.errors.push('websocket error');w.onclose=e=>window.socketTest.closed={code:e.code,reason:e.reason};},api);
  for(let i=0;i<20;i++){if(await page.evaluate(()=>window.socketTest.frames.some(f=>f.startsWith('CONNECTED')))){report.connected=true;break}await wait(250)}
  if(report.connected){
   await page.evaluate(()=>{window.socketTest.ws.send('SUBSCRIBE\nid:normal-user-queue\ndestination:/user/queue/messages\nack:auto\n\n\0');window.socketTest.ws.send('SUBSCRIBE\nid:legacy-client-path\ndestination:/user/2001/queue/messages\nack:auto\n\n\0');});
   await wait(500);
   const content='WebSocket delivery probe '+Date.now();
   const r=await fetch(api+'/api/chat/send',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({senderId:'1001',senderName:'Demo Student A',senderType:'STUDENT',receiverId:'2001',content})});report.httpSendStatus=r.status;
   await wait(5000);
   report.recipientDelivered=await page.evaluate(content=>window.socketTest.frames.some(f=>f.startsWith('MESSAGE')&&f.includes(content)),content);
   const history=await (await fetch(api+'/api/chat/history/1001/2001')).json();report.httpMessagePersisted=history.some(m=>m.content===content);
   const inbound='STOMP handler persistence probe '+Date.now();
   await page.evaluate(content=>window.socketTest.ws.send('SEND\ndestination:/app/chat.sendMessage\ncontent-type:application/json\n\n'+JSON.stringify({senderId:'1001',senderName:'Demo Student A',senderType:'STUDENT',receiverId:'2001',content})+'\0'),inbound);
   for(let i=0;i<20;i++){const h=await(await fetch(api+'/api/chat/history/1001/2001')).json();if(h.some(m=>m.content===inbound)){report.stompMessagePersisted=true;break}await wait(250)}
  }
  report.transportErrors=await page.evaluate(()=>({errors:window.socketTest.errors,closed:window.socketTest.closed}));
  await page.evaluate(()=>window.socketTest.ws.close());
 }finally{await browser.close()}
 report.verdict=report.recipientDelivered?'DELIVERED':'NOT_DELIVERED';
 fs.writeFileSync(path.join(root,'docs/websocket-validation.json'),JSON.stringify(report,null,2)+'\n');console.log(JSON.stringify(report,null,2));
})().catch(e=>{console.error(e.message);process.exitCode=1});
