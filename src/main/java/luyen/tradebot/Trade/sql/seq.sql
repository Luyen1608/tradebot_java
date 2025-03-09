--seq address
CREATE SEQUENCE IF NOT EXISTS public.tbl_address_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 32767
    CACHE 1;

ALTER SEQUENCE public.tbl_address_id_seq
    OWNED BY public.tbl_address.id;

ALTER SEQUENCE public.tbl_address_id_seq
    OWNER TO postgres;


--seq user
CREATE SEQUENCE IF NOT EXISTS public.tbl_user_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 32767
    CACHE 1;

ALTER SEQUENCE public.tbl_user_id_seq
    OWNED BY public.tbl_user.id;

ALTER SEQUENCE public.tbl_user_id_seq
    OWNER TO postgres;