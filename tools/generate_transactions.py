#!/usr/bin/env python3
"""Emit synthetic fraud evaluation requests as JSON Lines."""
import argparse, json, random, uuid
from datetime import datetime, timezone

def transaction(i):
    channel=random.choice(["POS","ECOM","ATM","UPI"])
    return {"transactionId":f"synthetic-{i}-{uuid.uuid4().hex[:8]}","customerId":f"customer-{random.randint(1,500)}","amount":round(random.choice([random.uniform(50,5000),random.uniform(5000,50000),random.uniform(50000,250000)]),2),"currency":"INR","channel":channel,"merchantCategory":random.choice(["5732","6011","6211","4829","5967","5411"]),"country":random.choices(["IN","US","GB","SG"],[.85,.05,.05,.05])[0],"newDevice":random.random()<.12,"timestamp":datetime.now(timezone.utc).isoformat().replace("+00:00","Z")}

def main():
 p=argparse.ArgumentParser();p.add_argument("-n","--count",type=int,default=20);p.add_argument("--seed",type=int);a=p.parse_args();random.seed(a.seed)
 for i in range(a.count): print(json.dumps(transaction(i+1),separators=(",",":")))
if __name__=="__main__":main()
