create extension if not exists "pgcrypto";

create table users (
                          id            uuid primary key default gen_random_uuid(),
                          email         varchar(320) not null unique,
                          password_hash varchar(255) not null,
                          display_name  varchar(120),
                          created_at    timestamptz  not null default now(),
                          updated_at    timestamptz  not null default now()
);

create table campaign (
                          id          uuid primary key default gen_random_uuid(),
                          owner_id    uuid         not null references users (id) on delete cascade,
                          name        varchar(160) not null,
                          status      varchar(24)  not null default 'DRAFT',
                          from_name   varchar(120) not null,
                          from_email  varchar(320) not null,
                          daily_cap   int          not null default 40,
                          time_zone   varchar(64)  not null default 'UTC',
                          created_at  timestamptz  not null default now(),
                          updated_at  timestamptz  not null default now(),
                          constraint campaign_status_chk
                              check (status in ('DRAFT', 'RUNNING', 'PAUSED', 'ARCHIVED'))
);

create index idx_campaign_owner on campaign (owner_id);

create table sequence_step (
                               id               uuid primary key default gen_random_uuid(),
                               campaign_id      uuid         not null references campaign (id) on delete cascade,
                               step_order       int          not null,
                               delay_days       int          not null default 0,
                               subject_template text         not null,
                               body_template    text         not null,
                               created_at       timestamptz  not null default now(),
                               constraint sequence_step_unique_order unique (campaign_id, step_order),
                               constraint sequence_step_delay_chk check (delay_days >= 0),
                               constraint sequence_step_order_chk check (step_order >= 1)
);

create table lead (
                      id           uuid primary key default gen_random_uuid(),
                      owner_id     uuid         not null references users (id) on delete cascade,
                      email        varchar(320) not null,
                      contact_name varchar(160),
                      company_name varchar(200),
                      website      varchar(500),
                      industry     varchar(120),
                      city         varchar(120),
                      region       varchar(120),
                      source       varchar(64)  not null default 'MANUAL',
                      enrichment   jsonb        not null default '{}'::jsonb,
                      created_at   timestamptz  not null default now(),
                      updated_at   timestamptz  not null default now()
);

create unique index lead_unique_email_per_owner on lead (owner_id, lower(email));

create index idx_lead_owner on lead (owner_id);

create table enrollment (
                            id                 uuid primary key default gen_random_uuid(),
                            campaign_id        uuid        not null references campaign (id) on delete cascade,
                            lead_id            uuid        not null references lead (id) on delete cascade,
                            status             varchar(24) not null default 'PENDING',
                            current_step_order int         not null default 0,
                            next_action_at     timestamptz,
                            started_at         timestamptz,
                            ended_at           timestamptz,
                            end_reason         varchar(255),
                            version            bigint      not null default 0,
                            created_at         timestamptz not null default now(),
                            updated_at         timestamptz not null default now(),
                            constraint enrollment_unique_lead unique (campaign_id, lead_id),
                            constraint enrollment_status_chk
                                check (status in ('PENDING', 'ACTIVE', 'COMPLETED', 'REPLIED', 'BOUNCED', 'UNSUBSCRIBED', 'FAILED'))
);

create index idx_enrollment_due
    on enrollment (next_action_at)
    where status = 'ACTIVE';

create table email_send (
                            id                  uuid primary key default gen_random_uuid(),
                            enrollment_id       uuid        not null references enrollment (id) on delete cascade,
                            step_id             uuid        not null references sequence_step (id) on delete cascade,
                            status              varchar(24) not null default 'QUEUED',
                            to_email            varchar(320) not null,
                            subject             text        not null,
                            body                text        not null,
                            provider_message_id varchar(255),
                            failure_reason      text,
                            attempt_count       int         not null default 0,
                            sent_at             timestamptz,
                            created_at          timestamptz not null default now(),
                            constraint email_send_idempotency unique (enrollment_id, step_id),
                            constraint email_send_status_chk
                                check (status in ('QUEUED', 'SENT', 'FAILED', 'SKIPPED'))
);

create table email_event (
                             id            uuid primary key default gen_random_uuid(),
                             email_send_id uuid        not null references email_send (id) on delete cascade,
                             event_type    varchar(32) not null,
                             occurred_at   timestamptz not null default now(),
                             payload       jsonb       not null default '{}'::jsonb,
                             constraint email_event_type_chk
                                 check (event_type in ('DELIVERED', 'OPENED', 'CLICKED', 'BOUNCED', 'COMPLAINED', 'REPLIED', 'UNSUBSCRIBED'))
);

create index idx_email_event_send on email_event (email_send_id);

create table suppression (
                             id         uuid primary key default gen_random_uuid(),
                             owner_id   uuid         not null references users (id) on delete cascade,
                             email      varchar(320) not null,
                             reason     varchar(32)  not null,
                             created_at timestamptz  not null default now(),
                             constraint suppression_reason_chk
                                 check (reason in ('UNSUBSCRIBED', 'HARD_BOUNCE', 'COMPLAINT', 'MANUAL'))
);

create unique index suppression_unique on suppression (owner_id, lower(email));