CREATE TABLE IF NOT EXISTS
  `accounts` (
    `uuid` TEXT,
    `name` TEXT,
    `salt` TEXT,
    `hashed_password` TEXT,
    `java` BOOLEAN DEFAULT 0,
    `bedrock` BOOLEAN DEFAULT 0,
    `joined` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `last_login` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`uuid`)
  )