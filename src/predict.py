import tensorflow as tf
from preprocess import *

model = tf.keras.models.load_model("../models/saved_model")

texts, _ = load_data("../data/sample_dataset.csv")
tokenizer = build_tokenizer(texts)


def predict(message):
    seq = tokenize(tokenizer, [message])
    prob = model.predict(seq)[0][0]

    label = "EMERGENCY" if prob > 0.5 else "NORMAL"

    print(label, prob)


predict("Please call ambulance now")
