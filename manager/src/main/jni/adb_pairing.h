#ifndef ADB_H
#define ADB_H

enum spake2_role_t {
    spake2_role_alice,
    spake2_role_bob,
};

using SPAKE2_CTX = void;

#define SPAKE2_MAX_MSG_SIZE 32
#define SPAKE2_MAX_KEY_SIZE 64

using EVP_MD = void;


using EVP_AEAD = void;

using ENGINE = void;

using EVP_AEAD_CTX = void;

#define EVP_AEAD_DEFAULT_TAG_LENGTH 0

#endif // ADB_H
