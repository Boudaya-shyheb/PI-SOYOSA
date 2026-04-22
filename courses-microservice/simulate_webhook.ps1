# Simulate Stripe Webhook for Course Enrollment Activation

$WEBHOOK_URL = "http://localhost:8081/api/payments/webhook"
$STRIPE_SIGNATURE = "t=123456789,v1=mock_signature" # This will fail signature verification if secret is set

# Payload for payment_intent.succeeded
$PAYLOAD = @{
    id = "evt_test_123"
    type = "payment_intent.succeeded"
    data = @{
        object = @{
            id = "pi_test_123"
            amount = 1000
            currency = "usd"
            status = "succeeded"
            metadata = @{
                userId = "student1"
                courseId = "PASTE_YOUR_COURSE_ID_HERE"
            }
        }
    }
} | ConvertTo-Json -Depth 10

Write-Host "Sending mock webhook to $WEBHOOK_URL..."

# Note: This will fail if stripe.webhook.secret is configured in application.yml
# because it requires a valid Stripe signature.
# To test without signature: temporarily comment out Webhook.constructEvent in PaymentController.
Invoke-RestMethod -Uri $WEBHOOK_URL -Method Post -Body $PAYLOAD -Headers @{"Stripe-Signature"=$STRIPE_SIGNATURE; "Content-Type"="application/json"}
