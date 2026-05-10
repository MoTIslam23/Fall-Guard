from store import DEVICES

# ---------------------------------------------------------
# Notification service
# In the prototype we print outgoing push notifications.
# Later, replace this with Firebase Admin SDK logic.
# ---------------------------------------------------------

async def send_push_to_owner(owner_type: str, owner_id: str, title: str, body: str, data: dict | None = None):
    targets = [
        device for device in DEVICES.values()
        if device["owner_type"] == owner_type and device["owner_id"] == owner_id
    ]

    for device in targets:
        print(
            f"[PUSH] to={device['push_token']} "
            f"title={title} body={body} data={data or {}}"
        )