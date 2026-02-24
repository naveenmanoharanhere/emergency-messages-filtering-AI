# Emergency Message Filtering AI App

email me @ naveenmanoharanhere@gmail.com for the dataset and model
A mobile application that automatically detects emergency messages and overrides Android Do Not Disturb (DND) mode to ensure urgent alerts are not missed.

The system uses a lightweight, quantized TensorFlow Lite model to classify incoming messages in real time.


## Problem Statement

Critical emergency messages are often missed when phones are in Do Not Disturb mode, especially during sleep or meetings.

This project aims to:
- Detect emergency intent in incoming messages
- Alert the user immediately by bypassing DND
- Minimize false positives to avoid unnecessary interruptions


## Key Features

- On-device NLP inference using TensorFlow Lite
- Emergency classification with high precision and recall
- Automatic DND override for urgent alerts
- Offline-first design for reliability


## Model Performance

- Precision: 92%
- Recall: 89%
- Dataset size: 5,000+ labeled messages


## Dataset

A custom supervised dataset was created containing:
- Emergency messages (medical, safety, urgent requests)
- Non-emergency personal and promotional messages

Messages were manually labeled to improve class balance and reduce bias.


## ML Pipeline

1. Text preprocessing and normalization
2. Tokenization and embedding
3. Binary classification model training
4. Post-training quantization
5. Conversion to TensorFlow Lite for mobile deployment
   

## Mobile Architecture

- Flutter frontend for UI and cross-platform structure
- Native Android integration for SMS access and DND control
- Background service for message interception and inference


## Permissions Used

- READ_SMS
- RECEIVE_SMS
- ACCESS_NOTIFICATION_POLICY

All permissions are used strictly for emergency detection.


## Tech Stack

- Flutter
- Android SDK (Kotlin)
- Python
- TensorFlow / TensorFlow Lite
- NLP
