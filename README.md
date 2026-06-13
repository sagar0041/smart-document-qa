# Smart Document Q&A

A document question-answering service built with **Spring Boot 3** and **Spring AI**, using
**retrieval-augmented generation (RAG)**: upload documents, ask questions in natural language,
and get answers grounded in your own content — with sources.

> Status: in active development. See the roadmap below.

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
| Messaging | Apache Kafka (KRaft) |
| AI models | OpenAI chat + embeddings via Spring AI |
| Testing | JUnit 5, Mockito, Testcontainers (PostgreSQL, Kafka) |
| Build / CI | Maven, GitHub Actions, Docker Compose |

## Running locally

```bash
# 1. start infrastructure (Postgres + pgvector, Kafka)
docker compose up -d

# 2. set your OpenAI key
export OPENAI_API_KEY=sk-...

# 3. run the service
./mvnw spring-boot:run
```

Health check: `curl localhost:8080/actuator/health`

## Roadmap

- [x] Project skeleton: Spring Boot 3, Docker Compose (pgvector, Kafka)
- [ ] Document upload endpoint with text extraction
- [ ] Kafka-based async ingestion pipeline (chunking + embeddings)
- [ ] Vector similarity search over pgvector
- [ ] RAG answer endpoint with source citations
- [ ] Integration tests with Testcontainers
- [ ] GitHub Actions CI (build + test on every push)
- [ ] API documentation (OpenAPI/Swagger)

## Why this project

Built to go deeper into production-style GenAI architecture on the Java stack: async
ingestion with Kafka, vector search tuning in pgvector, and grounded, citable LLM answers —
the same patterns behind enterprise RAG systems.
