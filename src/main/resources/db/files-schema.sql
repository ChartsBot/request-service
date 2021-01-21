CREATE TABLE IF NOT EXISTS FilesPaths (
    chatId INT NOT NULL,
    chatTitle VARCHAR(128),
    fileClassification VARCHAR(128) NOT NULL,
    fileType VARCHAR(128) NOT NULL,
	fileName VARCHAR(128) NOT NULL,
	author VARCHAR(128) NOT NULL,
	timeCreation INT,
	CONSTRAINT unique_meme UNIQUE(chatId, fileClassification, fileType, fileName)
	);