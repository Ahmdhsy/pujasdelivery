package com.example.pujasdelivery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pujasdelivery.data.ChatMessage
import com.example.pujasdelivery.viewmodel.ChatViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.observeAsState(initial = emptyList())
    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Warna abu-abu muda sesuai Figma
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White) // Background putih untuk header
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.AccountCircle, // Diperbaiki: Person -> AccountCircle
                contentDescription = "Profile Icon",
                modifier = Modifier.size(40.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Warung 01",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call",
                modifier = Modifier.size(24.dp),
                tint = Color(0xFFFFA500) // Warna oranye sesuai Figma
            )
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            reverseLayout = true // Tampilkan pesan terbaru di bawah
        ) {
            items(messages.reversed()) { message ->
                ChatMessageItem(
                    message = message,
                    isSentByUser = message.senderId == 1 // Asumsikan userId = 1 untuk contoh ini
                )
            }
        }

        // Input Pesan
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White) // Background putih untuk input
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = {
                    Text(
                        text = "Ketik pesan...",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(24.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                trailingIcon = {
                    Row {
                        Icon(
                            imageVector = Icons.Default.AddCircle, // Diperbaiki: Add -> AddCircle
                            contentDescription = "Add",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Camera, // Diperbaiki: CameraAlt -> Camera
                            contentDescription = "Camera",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFF1E3A8A), // Warna biru tua sesuai Figma
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Kirim Pesan",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage, isSentByUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isSentByUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 300.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSentByUser) Color(0xFF1E3A8A) else Color.White, // Biru tua untuk pesan pengguna, putih untuk pesan tenant
                contentColor = if (isSentByUser) Color.White else Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}