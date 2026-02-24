import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split

MAX_WORDS = 10000
MAX_LEN = 40


def load_data(path):
    df = pd.read_csv(path)

    texts = df["message"].astype(str).values
    labels = df["label"].values

    return texts, labels


def build_tokenizer(texts):
    tokenizer = tf.keras.preprocessing.text.Tokenizer(
        num_words=MAX_WORDS,
        oov_token="<OOV>"
    )

    tokenizer.fit_on_texts(texts)
    return tokenizer


def tokenize(tokenizer, texts):
    sequences = tokenizer.texts_to_sequences(texts)

    padded = tf.keras.preprocessing.sequence.pad_sequences(
        sequences,
        maxlen=MAX_LEN,
        padding="post"
    )

    return padded


def split_data(X, y):
    return train_test_split(X, y, test_size=0.2, random_state=42)
