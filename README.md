# bigdata-traffic-database
대용량 트래픽 처리를 위한 데이터베이스 구축 프로젝트
- SpringBoot + Mybatis + jwt

# Project Wokr Flow

### 1. 광고 클릭 Flow
![img.png](img.png)

### 2. 통계 집계 배치 Flow
![img_1.png](img_1.png)

## Elastic Search - Kibana 쿼리문
```
# 인덱스 설정
PUT /article_new
{
  "settings": {
    "analysis": {
      "analyzer": {
        "nori_analyzer": {
          "type": "custom",
          "tokenizer": "nori_tokenizer",
          "filter": [
            "nori_part_of_speech",
            "nori_readingform",
            "lowercase",
            "cjk_width"
          ]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "content": {
        "type": "text",
        "analyzer": "nori_analyzer"
      },
      "created_date": {
        "type": "date",
        "format": "yyyy-MM-dd'T'HH:mm:ss.SSS"
      },
      "title": {
        "type": "text",
        "analyzer": "nori_analyzer",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      },
      "updated_date": {
        "type": "date",
        "format": "yyyy-MM-dd'T'HH:mm:ss.SSS"
      },
      "user_id": {
        "type": "long"
      },
      "user_name": {
        "type": "text",
        "analyzer": "nori_analyzer"
      },
      "board_id": {
        "type": "long"
      },
      "is_deleted": {
        "type": "boolean"
      }
    }
  }
}

# index 목록 및 상태 조회
GET /_cat/indices

# reindex
POST /_reindex
{
  "source": {
    "index": "article"
  },
  "dest": {
    "index": "article_new"
  },
  "script": {
    "source": "ctx._source.remove('id')"
  }
}

# 기존의 article index 제거
DELETE /article

# index aliase 변경하기
POST /_aliases
{
  "actions": [
    {
      "add": {
        "index": "article_new",
        "alias": "article"
      }
    }
  ]
}

# article_new -> article로 별칭이 부여되서 article로 조회해도 article_new로 조회가 된다.
GET /article/_search

# replicas 세팅 변경하기
PUT /article_new/_settings
{
  "index": {
    "number_of_replicas": 0
  }
}

POST /article/_doc/3
{
  "content": "개발 티켓 스프린트",
  "created_date": "2024-06-03T16:23:56.041",
  "title": "테스트 - 3",
  "updated_date": null,
  "user_id": 10,
  "user_name": "jinyeong",
  "board_id": 1,
  "is_deleted": false
}

GET /article/_doc

# index 전체 데이터 조회
GET /article/_search

GET /article_new/_search

# index id별로 데이터 조회
GET /article/_doc/8

# index id별로 삭제 요청
DELETE /article/_doc/3

GET /article/_search
{
  "query": {
    "match": {
      "content": "개발자"
    }
  }
}

GET /article/_analyze
{
  "analyzer": "nori_analyzer",
  "text": "개"
}

# 광고 조회수 집계 인덱스 조회
GET /ad_view_stat/_search
```

### MySQL 쿼리문
```mysql
GRANT ALL PRIVILEGES ON bigdata.* TO 'myuser'@'%';
FLUSH PRIVILEGES;

CREATE TABLE user
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(20)  NOT NULL,
    password     VARCHAR(100) NOT NULL,
    email        VARCHAR(50)  NOT NULL,
    last_login   DATETIME,
    created_date DATETIME     NOT NULL,
    updated_date DATETIME
);

CREATE TABLE jwt_blacklist
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    token           VARCHAR(255) NOT NULL,
    expiration_time DATETIME     NOT NULL,
    username        VARCHAR(20)  NOT NULL
);

CREATE TABLE article
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    content      MEDIUMTEXT   NOT NULL,
    user_id      BIGINT       NOT NULL,
    board_id     BIGINT       NOT NULL,
    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_date DATETIME     NOT NULL,
    updated_date DATETIME,
    view_count   BIGINT       NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_board_id (board_id)
);

CREATE TABLE board
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    description  TEXT         NOT NULL,
    created_date DATETIME     NOT NULL,
    updated_date DATETIME
);

CREATE TABLE comment
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    content      MEDIUMTEXT NOT NULL,
    author_id    BIGINT,
    article_id   BIGINT,
    is_deleted   BOOLEAN    NOT NULL DEFAULT FALSE,
    created_date DATETIME   NOT NULL,
    updated_date DATETIME
);
```
