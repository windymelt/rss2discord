# rss2discord
Post latest RSS post to Discord!

This software is intended to be used as AWS Lambda Function.

## How to post RSS

Use released JAR file for Lambda function.

The function requires two environment variables:

- `RSS_URL` for feed URL e.g. `https://scala.epfl.ch/feed`
- `WEBHOOK_URL` for Discord webhook URL e.g. `"https://discord.com/api/webhooks/1234567890987654321`

Handler should be `io.github.windymelt.rss2discord.Rss2Discord:handler`.

This function does not have state: You must call this function **every 30 minitues** typically with EventBridge.

This function requires some time to run: You may specify timeout to 1 minutes.
