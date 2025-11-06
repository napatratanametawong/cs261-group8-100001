# Reservation Document

```bash
POST /api/reservations 
Authorization: Bearer <token>
```

Don't forget to check token with GET ```/auth/me```

Example input:
```bash
{
  "roomCode": "LC2-107",
  "reservationDate": "2025-11-06",
  "slotCodes": ["S1330_1500"],
  "reason": "Project kickoff",
  "fileAttachment": null,
  "userEmail": "teetad.aum@dome.tu.ac.th",
  "userName": ""
}
```

Result (200 OK):
```bash
{
  "reservationId": 20002,
  "roomCode": "LC2-107",
  "reservationDate": "2025-11-06",
  "reason": "Project kickoff",
  "fileAttachment": null,
  "step": "SUBMITTED",
  "finalStatus": "PENDING",
  "userEmail": "teetad.aum@dome.tu.ac.th",
  "userName": "",
  "staffReviewerEmail": null,
  "staffReviewedAt": null,
  "headApproverEmail": null,
  "headDecidedAt": null,
  "returnReason": null,
  "rejectReason": null,
  "cancelReason": null,
  "approvedAt": null,
  "createdAt": "2025-11-06T10:18:21.178012506+07:00",
  "slots": [
    { "slotCode": "S1330_1500", "isActive": true }
  ]
}
```

Result (500 error, if full):
```bash
{
    "path": "/api/reservations",
    "error": "IllegalStateException",
    "message": "ในเวลา 13:30 - 15:00 ถูกจองแล้ว"
}