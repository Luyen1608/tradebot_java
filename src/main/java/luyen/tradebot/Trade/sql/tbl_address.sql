-- Table: public.tbl_address

-- DROP TABLE IF EXISTS public.tbl_address;

CREATE TABLE IF NOT EXISTS public.tbl_address
(
    id integer NOT NULL DEFAULT nextval('tbl_address_id_seq'::regclass),
    apartment_number character varying(255) COLLATE pg_catalog."default",
    floor character varying(255) COLLATE pg_catalog."default",
    building character varying(255) COLLATE pg_catalog."default",
    street_number character varying(255) COLLATE pg_catalog."default",
    street character varying(255) COLLATE pg_catalog."default",
    city character varying(255) COLLATE pg_catalog."default",
    country character varying(255) COLLATE pg_catalog."default",
    address_type integer,
    user_id integer,
    created_at date NOT NULL,
    updated_at date NOT NULL,
    CONSTRAINT tbl_address_pkey PRIMARY KEY (id)
    )

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.tbl_address
    OWNER to postgres;