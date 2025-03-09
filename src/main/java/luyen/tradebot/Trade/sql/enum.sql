CREATE TYPE public.e_gender AS ENUM
    ('MALE', 'FEMALE', 'OTHER');

ALTER TYPE public.e_gender
    OWNER TO postgres;



CREATE TYPE public.e_user_status AS ENUM
    ('ACTIVE', 'INACTIVE', 'DELETED', 'NONE');

ALTER TYPE public.e_user_status
    OWNER TO postgres;



CREATE TYPE public.e_user_type AS ENUM
    ('ADMIN', 'USER', 'OTHER');

ALTER TYPE public.e_user_type
    OWNER TO postgres;
