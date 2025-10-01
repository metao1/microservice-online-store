package com.metao.book.shared.domain.base;

import com.google.protobuf.Message;

public record TranslationResult(String key, Message message) {
}
