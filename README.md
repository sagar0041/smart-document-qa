# Smart Document Q&A

[![CI](https://github.com/sagar0041/smart-document-qa/actions/workflows/ci.yml/badge.svg)](https://github.com/sagar0041/smart-document-qa/actions/workflows/ci.yml)

A document question-answering service built with **Spring Boot 3** and **Spring AI**, using
**retrieval-augmented generation (RAG)**: upload documents, ask questions in natural language,
and get answers grounded in your own content — with sources.

> Status: core pipeline complete. RAG answer endpoint working end-to-end with local dev profile.

## What this demonstrates
- Production-style RAG architecture with Spring AI (retrieval, grounding, source citations)
- Decoupled, async processing using Kafka for document ingestion
- Vector similarity search with pgvector, including index tuning (HNSW)
- Integration testing against real infrastructure with Testcontainers
- End-to-end ownership: architecture, implementation, testing, and CI

## How it works

```
                 ┌──────────────┐        ┌─────────────────┐
  upload ──────► │  REST API    │ ─────► │  Kafka topic     │
                 │ (Spring Boot)│        │  doc-ingestion   │
                 └──────┬───────┘        └────────┬─────────┘
                        │                         │ async
  question ────────────►│                ┌────────▼─────────┐
                        │                │ Ingestion worker  │
                 ┌──────▼───────┐        │ chunk + embed     │
                 │ RAG pipeline │        └────────┬─────────┘
                 │ retrieve +   │                 │
                 │ generate     │        ┌────────▼─────────┐
                 └──────┬───────┘ ◄──────│ PostgreSQL        │
                        │     similarity │ + pgvector (HNSW) │
  answer + sources ◄────┘        search  └──────────────────┘
```

1. **Ingestion** — documents are uploaded via REST and published to a Kafka topic; an async
   consumer chunks the text, creates embeddings (OpenAI `text-embedding-3-small`), and stores
   them in PostgreSQL with the pgvector extension (HNSW index, cosine distance).
2. **Retrieval** — a question is embedded and the most similar chunks are fetched via vector
   similarity search.
3. **Generation** — the retrieved chunks are passed as context to the chat model
   (`gpt-4o-mini`), which answers strictly from the provided sources and cites them.

## Tech stack

| Area | Technology |
|---|---|
| Framework | Java 17, Spring Boot 3.4, Spring AI 1.0 |
| Vector store | PostgreSQL 16 + pgvector |
| Messaging | Apache Kafka (Zookeeper-based, Confluent images) |
| AI models | OpenAI chat + embeddings via Spring AI |
| Testing | JUnit 5, Mockito, Testcontainers (PostgreSQL, Kafka) |
| Build / CI | Maven, GitHub Actions, Docker Compose |

## Running locally

```bash
# 1. start infrastructure (Postgres + pgvector, Zookeeper, Kafka)
docker compose up -d

# 2a. with a real OpenAI key (full semantic search + RAG)
export OPENAI_API_KEY=sk-...
./mvnw spring-boot:run

# 2b. without an OpenAI key (local dev mode — fake embeddings, no API cost)
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

Health check: `curl localhost:8080/actuator/health`

## API endpoints

### Upload a document
```bash
curl -X POST -F "file=@document.txt" localhost:8080/api/documents
# returns: { "id": "...", "filename": "...", "status": "UPLOADED", "extractedChars": N }
```

### Search — retrieve similar chunks
```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"question":"What is Kafka?"}' \
  localhost:8080/api/search
# returns: [{ "content": "...", "documentId": "...", "score": 0.74 }]
```

### Ask — RAG answer with source citations
```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"question":"What is Kafka?"}' \
  localhost:8080/api/ask
# returns: { "answer": "...", "sources": [{ "content": "...", "documentId": "...", "score": 0.74 }] }
```

### Check document processing status
```bash
# Status flow: UPLOADED → PROCESSING → INDEXED (or FAILED)
docker exec -it smart-document-qa-postgres-1 psql -U docqa -d docqa \
  -c "SELECT id, filename, status FROM documents ORDER BY created_at DESC LIMIT 5;"
```

## Roadmap

- [x] Project skeleton: Spring Boot 3, Docker Compose (pgvector, Kafka)
- [x] Document upload endpoint with Apache Tika text extraction
- [x] Kafka producer: publish document ID on upload
- [x] Kafka consumer: chunking + embeddings + pgvector storage
- [x] Vector similarity search over pgvector
- [x] RAG answer endpoint with source citations
- [x] Local dev profile: fake embedding model (no OpenAI cost)
- [x] Integration tests with Testcontainers
- [x] GitHub Actions CI (build + test on every push)

## Why this project

Built to go deeper into production-style GenAI architecture on the Java stack: async
ingestion with Kafka, vector search tuning in pgvector, and grounded, citable LLM answers —
the same patterns behind enterprise RAG systems.