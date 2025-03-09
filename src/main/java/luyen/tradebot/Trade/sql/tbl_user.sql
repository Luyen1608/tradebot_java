-- Table: public.tbl_user

-- DROP TABLE IF EXISTS public.tbl_user;

CREATE TABLE IF NOT EXISTS public.tbl_user
(
    id integer NOT NULL DEFAULT nextval('tbl_user_id_seq'::regclass),
    first_name character varying(255) COLLATE pg_catalog."default",
    last_name character varying(255) COLLATE pg_catalog."default",
    date_of_birth date,
    gender e_gender,
    phone character varying(255) COLLATE pg_catalog."default",
    email character varying(255) COLLATE pg_catalog."default",
    user_name character varying(255) COLLATE pg_catalog."default",
    password character varying(255) COLLATE pg_catalog."default",
    user_status e_user_status,
    user_type e_user_type,
    created_at date,
    updated_at date,
    CONSTRAINT tbl_user_pkey PRIMARY KEY (id)
    )

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.tbl_user
    OWNER to postgres;