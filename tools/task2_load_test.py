#!/usr/bin/env python3
"""Task 2 smoke/load test: provisions 50 active rules then runs 2,000 evals at concurrency 20."""
import concurrent.futures, json, os, statistics, time, urllib.request, uuid
BASE=os.getenv("BASE_URL","http://localhost:8080")
def request(path, body=None, method=None):
    data=None if body is None else json.dumps(body).encode()
    req=urllib.request.Request(BASE+path,data=data,headers={"Content-Type":"application/json"},method=method)
    with urllib.request.urlopen(req,timeout=10) as r: return json.loads(r.read()) if r.status != 204 else {}
def provision():
    for i in range(50):
        created=request("/api/v1/rules",{"name":"task2-load-"+uuid.uuid4().hex[:12],"action":"REVIEW","priority":1000+i,
          "condition":{"field":"amount","op":"gte","value":10**9+i}})
        rid=created.get("ruleId") or created.get("id")
        version=created.get("version",1)
        request(f"/api/v1/rules/{rid}/versions/{version}/activate",{}, "POST")
def evaluate(i):
    payload={"transactionId":"load-"+uuid.uuid4().hex,"customerId":"load-customer-"+str(i%100),
      "amount":1234.56,"currency":"INR","channel":"UPI","merchantCategory":"5411","country":"IN",
      "newDevice":False,"timestamp":time.strftime("%Y-%m-%dT%H:%M:%SZ",time.gmtime())}
    start=time.perf_counter()
    request("/api/v1/evaluate",payload,"POST")
    return (time.perf_counter()-start)*1000
def percentile(values,p):
    values=sorted(values); return values[min(len(values)-1,max(0,int((len(values)-1)*p)))]
if __name__=="__main__":
    print("Provisioning 50 active rules...")
    provision()
    print("Sending 2,000 evaluations with concurrency=20...")
    with concurrent.futures.ThreadPoolExecutor(max_workers=20) as pool:
        latencies=list(pool.map(evaluate,range(2000)))
    print(f"count={len(latencies)} p50={percentile(latencies,.50):.2f}ms p95={percentile(latencies,.95):.2f}ms")
