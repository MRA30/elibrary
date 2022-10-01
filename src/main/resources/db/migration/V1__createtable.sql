CREATE TABLE IF NOT EXISTS Users (
    id SERIAL PRIMARY KEY,
    number_identity VARCHAR(13) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    enabled BOOLEAN,
    gender VARCHAR(50) NOT NULL,
    no_hp VARCHAR(15) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    user_role VARCHAR(50) NOT NULL
    );

CREATE TABLE IF NOT EXISTS Categories(
    id SERIAL NOT NULL,
    category VARCHAR(50) NOT NULL,
    PRIMARY KEY(id)
    );

CREATE TABLE IF NOT EXISTS Books(
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100) NOT NULL,
    year_publication VARCHAR(4) NOT NULL,
    quantity INTEGER NOT NULL,
    category_id BIGINT,
    synopsis TEXT,
    FOREIGN KEY(category_id) REFERENCES Categories(id)
    );

CREATE TABLE IF NOT EXISTS Borrows(
    id SERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    borrow_date DATE NOT NULL,
    return_date DATE,
    is_returned BOOLEAN DEFAULT FALSE,
    penalty NUMERIC(5,2),
    description TEXT,
    FOREIGN KEY(book_id) REFERENCES Books(id),
    FOREIGN KEY(user_id) REFERENCES Users(id)
    );

CREATE TABLE IF NOT EXISTS Book_requests(
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    available BOOLEAN DEFAULT FALSE,
    description VARCHAR(255),
    FOREIGN KEY(user_id) REFERENCES Users(id)
    );