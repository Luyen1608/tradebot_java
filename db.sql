PGDMP  4                    }         	   trade_bot    16.8    16.8 B    B           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                      false            C           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                      false            D           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                      false            E           1262    16398 	   trade_bot    DATABASE     l   CREATE DATABASE trade_bot WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'vi';
    DROP DATABASE trade_bot;
                postgres    false                        2615    2200    public    SCHEMA        CREATE SCHEMA public;
    DROP SCHEMA public;
                pg_database_owner    false            F           0    0    SCHEMA public    COMMENT     6   COMMENT ON SCHEMA public IS 'standard public schema';
                   pg_database_owner    false    4            p           1247    16542    e_account_status    TYPE     j   CREATE TYPE public.e_account_status AS ENUM (
    'CONNECT',
    'DISCONNECT',
    'ERROR',
    'NONE'
);
 #   DROP TYPE public.e_account_status;
       public          postgres    false    4            s           1247    16552    e_account_type    TYPE     F   CREATE TYPE public.e_account_type AS ENUM (
    'DEMO',
    'LIVE'
);
 !   DROP TYPE public.e_account_type;
       public          postgres    false    4            v           1247    16558    e_action_trading    TYPE     x   CREATE TYPE public.e_action_trading AS ENUM (
    'ENTER_LONG',
    'ENTER_SHORT',
    'EXIT_LONG',
    'EXIT_SHORT'
);
 #   DROP TYPE public.e_action_trading;
       public          postgres    false    4            y           1247    16568 
   e_bot_from    TYPE     e   CREATE TYPE public.e_bot_from AS ENUM (
    'OKX',
    'Binance',
    'Bybit',
    'CoinstratPro'
);
    DROP TYPE public.e_bot_from;
       public          postgres    false    4            |           1247    16578    e_bot_status    TYPE     V   CREATE TYPE public.e_bot_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'NONE'
);
    DROP TYPE public.e_bot_status;
       public          postgres    false    4                       1247    16586    e_connect_status    TYPE     t   CREATE TYPE public.e_connect_status AS ENUM (
    'DISCONNECTED',
    'CONNECTING',
    'CONNECTED',
    'ERROR'
);
 #   DROP TYPE public.e_connect_status;
       public          postgres    false    4            U           1247    16420    e_gender    TYPE     O   CREATE TYPE public.e_gender AS ENUM (
    'MALE',
    'FEMALE',
    'OTHER'
);
    DROP TYPE public.e_gender;
       public          postgres    false    4            X           1247    16428    e_user_status    TYPE     f   CREATE TYPE public.e_user_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'DELETED',
    'NONE'
);
     DROP TYPE public.e_user_status;
       public          postgres    false    4            [           1247    16438    e_user_type    TYPE     Q   CREATE TYPE public.e_user_type AS ENUM (
    'ADMIN',
    'USER',
    'OTHER'
);
    DROP TYPE public.e_user_type;
       public          postgres    false    4            �            1259    16512    tbl_accounts    TABLE     �  CREATE TABLE public.tbl_accounts (
    id integer NOT NULL,
    account_id character varying(255),
    client_id character varying(255) NOT NULL,
    client_secret character varying(255) NOT NULL,
    access_token text,
    refresh_token text,
    token_expiry date,
    is_active boolean DEFAULT false,
    is_connected boolean DEFAULT false,
    last_connected date,
    error_message text,
    bot_id bigint,
    account_name character varying(255),
    connection_status public.e_account_status,
    ctid_trader_account_id integer,
    type_account public.e_account_type,
    created_at date,
    updated_at date,
    authenticated boolean
);
     DROP TABLE public.tbl_accounts;
       public         heap    postgres    false    4    880    883            �            1259    16511    tbl_accounts_id_seq    SEQUENCE     �   CREATE SEQUENCE public.tbl_accounts_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 *   DROP SEQUENCE public.tbl_accounts_id_seq;
       public          postgres    false    224    4            G           0    0    tbl_accounts_id_seq    SEQUENCE OWNED BY     K   ALTER SEQUENCE public.tbl_accounts_id_seq OWNED BY public.tbl_accounts.id;
          public          postgres    false    223            �            1259    16449    tbl_address    TABLE     �  CREATE TABLE public.tbl_address (
    apartment_number character varying(255),
    floor character varying(255),
    building character varying(255),
    street_number character varying(255),
    street character varying(255),
    city character varying(255),
    country character varying(255),
    address_type integer,
    user_id integer,
    created_at date NOT NULL,
    updated_at date NOT NULL,
    id smallint NOT NULL
);
    DROP TABLE public.tbl_address;
       public         heap    postgres    false    4            �            1259    16468    tbl_address_id_seq    SEQUENCE     �   CREATE SEQUENCE public.tbl_address_id_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 )   DROP SEQUENCE public.tbl_address_id_seq;
       public          postgres    false    215    4            H           0    0    tbl_address_id_seq    SEQUENCE OWNED BY     I   ALTER SEQUENCE public.tbl_address_id_seq OWNED BY public.tbl_address.id;
          public          postgres    false    218            �            1259    16491    tbl_bots    TABLE     �  CREATE TABLE public.tbl_bots (
    id integer NOT NULL,
    bot_name character varying(255) NOT NULL,
    description text,
    is_active boolean DEFAULT false,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    signal_token character varying(255),
    status public.e_bot_status,
    number_account integer,
    max_account integer,
    exchange character varying(255),
    bot_from public.e_bot_from
);
    DROP TABLE public.tbl_bots;
       public         heap    postgres    false    889    4    892            �            1259    16490    tbl_bots_id_seq    SEQUENCE     �   CREATE SEQUENCE public.tbl_bots_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 &   DROP SEQUENCE public.tbl_bots_id_seq;
       public          postgres    false    4    222            I           0    0    tbl_bots_id_seq    SEQUENCE OWNED BY     C   ALTER SEQUENCE public.tbl_bots_id_seq OWNED BY public.tbl_bots.id;
          public          postgres    false    221            �            1259    16596    tbl_order_positions    TABLE     �   CREATE TABLE public.tbl_order_positions (
    id integer NOT NULL,
    position_id character varying(255),
    status character varying(255),
    error_message character varying(255)
);
 '   DROP TABLE public.tbl_order_positions;
       public         heap    postgres    false    4            �            1259    16595    tbl_order_positions_id_seq    SEQUENCE     �   CREATE SEQUENCE public.tbl_order_positions_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 1   DROP SEQUENCE public.tbl_order_positions_id_seq;
       public          postgres    false    4    228            J           0    0    tbl_order_positions_id_seq    SEQUENCE OWNED BY     Y   ALTER SEQUENCE public.tbl_order_positions_id_seq OWNED BY public.tbl_order_positions.id;
          public          postgres    false    227            �            1259    16528 
   tbl_orders    TABLE     �  CREATE TABLE public.tbl_orders (
    id integer NOT NULL,
    symbol_id character varying(50) NOT NULL,
    trade_side character varying(10) NOT NULL,
    volume numeric(19,8) NOT NULL,
    status character varying(20) NOT NULL,
    open_time timestamp without time zone,
    close_time timestamp without time zone,
    order_type character varying(20),
    account_id bigint,
    comment character varying(255)
);
    DROP TABLE public.tbl_orders;
       public         heap    postgres    false    4            �            1259    16527    tbl_orders_id_seq    SEQUENCE     �   CREATE SEQUENCE public.tbl_orders_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 (   DROP SEQUENCE public.tbl_orders_id_seq;
       public          postgres    false    226    4            K           0    0    tbl_orders_id_seq    SEQUENCE OWNED BY     G   ALTER SEQUENCE public.tbl_orders_id_seq OWNED BY public.tbl_orders.id;
          public          postgres    false    225            �            1259    16477 	   tbl_token    TABLE       CREATE TABLE public.tbl_token (
    id smallint NOT NULL,
    username character varying(255),
    access_token character varying(255),
    refresh_token character varying(255),
    reset_token character varying(255),
    created_at date,
    updated_at date
);
    DROP TABLE public.tbl_token;
       public         heap    postgres    false    4            �            1259    16480    tbl_token_id_seq    SEQUENCE     �   CREATE SEQUENCE public.tbl_token_id_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 '   DROP SEQUENCE public.tbl_token_id_seq;
       public          postgres    false    4    219            L           0    0    tbl_token_id_seq    SEQUENCE OWNED BY     E   ALTER SEQUENCE public.tbl_token_id_seq OWNED BY public.tbl_token.id;
          public          postgres    false    220            �            1259    16454    tbl_user    TABLE     �  CREATE TABLE public.tbl_user (
    first_name character varying(255),
    last_name character varying(255),
    date_of_birth date,
    gender public.e_gender,
    phone character varying(255),
    email character varying(255),
    user_name character varying(255),
    password character varying(255),
    user_status public.e_user_status,
    user_type public.e_user_type,
    created_at date,
    updated_at date,
    id smallint NOT NULL
);
    DROP TABLE public.tbl_user;
       public         heap    postgres    false    853    4    859    856            �            1259    16459    tbl_user_id_seq    SEQUENCE     �   CREATE SEQUENCE public.tbl_user_id_seq
    AS smallint
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 &   DROP SEQUENCE public.tbl_user_id_seq;
       public          postgres    false    4    216            M           0    0    tbl_user_id_seq    SEQUENCE OWNED BY     C   ALTER SEQUENCE public.tbl_user_id_seq OWNED BY public.tbl_user.id;
          public          postgres    false    217            �           2604    16643    tbl_accounts id    DEFAULT     r   ALTER TABLE ONLY public.tbl_accounts ALTER COLUMN id SET DEFAULT nextval('public.tbl_accounts_id_seq'::regclass);
 >   ALTER TABLE public.tbl_accounts ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    224    223    224            �           2604    16644    tbl_address id    DEFAULT     p   ALTER TABLE ONLY public.tbl_address ALTER COLUMN id SET DEFAULT nextval('public.tbl_address_id_seq'::regclass);
 =   ALTER TABLE public.tbl_address ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    218    215            �           2604    16645    tbl_bots id    DEFAULT     j   ALTER TABLE ONLY public.tbl_bots ALTER COLUMN id SET DEFAULT nextval('public.tbl_bots_id_seq'::regclass);
 :   ALTER TABLE public.tbl_bots ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    221    222    222            �           2604    16646    tbl_order_positions id    DEFAULT     �   ALTER TABLE ONLY public.tbl_order_positions ALTER COLUMN id SET DEFAULT nextval('public.tbl_order_positions_id_seq'::regclass);
 E   ALTER TABLE public.tbl_order_positions ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    228    227    228            �           2604    16647    tbl_orders id    DEFAULT     n   ALTER TABLE ONLY public.tbl_orders ALTER COLUMN id SET DEFAULT nextval('public.tbl_orders_id_seq'::regclass);
 <   ALTER TABLE public.tbl_orders ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    226    225    226            �           2604    16648    tbl_token id    DEFAULT     l   ALTER TABLE ONLY public.tbl_token ALTER COLUMN id SET DEFAULT nextval('public.tbl_token_id_seq'::regclass);
 ;   ALTER TABLE public.tbl_token ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    220    219            �           2604    16649    tbl_user id    DEFAULT     j   ALTER TABLE ONLY public.tbl_user ALTER COLUMN id SET DEFAULT nextval('public.tbl_user_id_seq'::regclass);
 :   ALTER TABLE public.tbl_user ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    217    216            ;          0    16512    tbl_accounts 
   TABLE DATA           )  COPY public.tbl_accounts (id, account_id, client_id, client_secret, access_token, refresh_token, token_expiry, is_active, is_connected, last_connected, error_message, bot_id, account_name, connection_status, ctid_trader_account_id, type_account, created_at, updated_at, authenticated) FROM stdin;
    public          postgres    false    224   �P       2          0    16449    tbl_address 
   TABLE DATA           �   COPY public.tbl_address (apartment_number, floor, building, street_number, street, city, country, address_type, user_id, created_at, updated_at, id) FROM stdin;
    public          postgres    false    215   �Q       9          0    16491    tbl_bots 
   TABLE DATA           �   COPY public.tbl_bots (id, bot_name, description, is_active, created_at, updated_at, signal_token, status, number_account, max_account, exchange, bot_from) FROM stdin;
    public          postgres    false    222   �R       ?          0    16596    tbl_order_positions 
   TABLE DATA           U   COPY public.tbl_order_positions (id, position_id, status, error_message) FROM stdin;
    public          postgres    false    228   �R       =          0    16528 
   tbl_orders 
   TABLE DATA           �   COPY public.tbl_orders (id, symbol_id, trade_side, volume, status, open_time, close_time, order_type, account_id, comment) FROM stdin;
    public          postgres    false    226   S       6          0    16477 	   tbl_token 
   TABLE DATA           s   COPY public.tbl_token (id, username, access_token, refresh_token, reset_token, created_at, updated_at) FROM stdin;
    public          postgres    false    219   2S       3          0    16454    tbl_user 
   TABLE DATA           �   COPY public.tbl_user (first_name, last_name, date_of_birth, gender, phone, email, user_name, password, user_status, user_type, created_at, updated_at, id) FROM stdin;
    public          postgres    false    216   OS       N           0    0    tbl_accounts_id_seq    SEQUENCE SET     B   SELECT pg_catalog.setval('public.tbl_accounts_id_seq', 17, true);
          public          postgres    false    223            O           0    0    tbl_address_id_seq    SEQUENCE SET     A   SELECT pg_catalog.setval('public.tbl_address_id_seq', 15, true);
          public          postgres    false    218            P           0    0    tbl_bots_id_seq    SEQUENCE SET     =   SELECT pg_catalog.setval('public.tbl_bots_id_seq', 1, true);
          public          postgres    false    221            Q           0    0    tbl_order_positions_id_seq    SEQUENCE SET     I   SELECT pg_catalog.setval('public.tbl_order_positions_id_seq', 1, false);
          public          postgres    false    227            R           0    0    tbl_orders_id_seq    SEQUENCE SET     @   SELECT pg_catalog.setval('public.tbl_orders_id_seq', 1, false);
          public          postgres    false    225            S           0    0    tbl_token_id_seq    SEQUENCE SET     ?   SELECT pg_catalog.setval('public.tbl_token_id_seq', 1, false);
          public          postgres    false    220            T           0    0    tbl_user_id_seq    SEQUENCE SET     >   SELECT pg_catalog.setval('public.tbl_user_id_seq', 11, true);
          public          postgres    false    217            �           2606    16521    tbl_accounts tbl_accounts_pkey 
   CONSTRAINT     \   ALTER TABLE ONLY public.tbl_accounts
    ADD CONSTRAINT tbl_accounts_pkey PRIMARY KEY (id);
 H   ALTER TABLE ONLY public.tbl_accounts DROP CONSTRAINT tbl_accounts_pkey;
       public            postgres    false    224            �           2606    16476    tbl_address tbl_address_pkey 
   CONSTRAINT     Z   ALTER TABLE ONLY public.tbl_address
    ADD CONSTRAINT tbl_address_pkey PRIMARY KEY (id);
 F   ALTER TABLE ONLY public.tbl_address DROP CONSTRAINT tbl_address_pkey;
       public            postgres    false    215            �           2606    16499    tbl_bots tbl_bots_pkey 
   CONSTRAINT     T   ALTER TABLE ONLY public.tbl_bots
    ADD CONSTRAINT tbl_bots_pkey PRIMARY KEY (id);
 @   ALTER TABLE ONLY public.tbl_bots DROP CONSTRAINT tbl_bots_pkey;
       public            postgres    false    222            �           2606    16603 ,   tbl_order_positions tbl_order_positions_pkey 
   CONSTRAINT     j   ALTER TABLE ONLY public.tbl_order_positions
    ADD CONSTRAINT tbl_order_positions_pkey PRIMARY KEY (id);
 V   ALTER TABLE ONLY public.tbl_order_positions DROP CONSTRAINT tbl_order_positions_pkey;
       public            postgres    false    228            �           2606    16535    tbl_orders tbl_orders_pkey 
   CONSTRAINT     X   ALTER TABLE ONLY public.tbl_orders
    ADD CONSTRAINT tbl_orders_pkey PRIMARY KEY (id);
 D   ALTER TABLE ONLY public.tbl_orders DROP CONSTRAINT tbl_orders_pkey;
       public            postgres    false    226            �           2606    16488    tbl_token tbl_token_pkey 
   CONSTRAINT     V   ALTER TABLE ONLY public.tbl_token
    ADD CONSTRAINT tbl_token_pkey PRIMARY KEY (id);
 B   ALTER TABLE ONLY public.tbl_token DROP CONSTRAINT tbl_token_pkey;
       public            postgres    false    219            �           2606    16467    tbl_user tbl_user_pkey 
   CONSTRAINT     T   ALTER TABLE ONLY public.tbl_user
    ADD CONSTRAINT tbl_user_pkey PRIMARY KEY (id);
 @   ALTER TABLE ONLY public.tbl_user DROP CONSTRAINT tbl_user_pkey;
       public            postgres    false    216            �           2606    16522 %   tbl_accounts tbl_accounts_bot_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.tbl_accounts
    ADD CONSTRAINT tbl_accounts_bot_id_fkey FOREIGN KEY (bot_id) REFERENCES public.tbl_bots(id) ON DELETE CASCADE;
 O   ALTER TABLE ONLY public.tbl_accounts DROP CONSTRAINT tbl_accounts_bot_id_fkey;
       public          postgres    false    222    4762    224            �           2606    16536 %   tbl_orders tbl_orders_account_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.tbl_orders
    ADD CONSTRAINT tbl_orders_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.tbl_accounts(id) ON DELETE CASCADE;
 O   ALTER TABLE ONLY public.tbl_orders DROP CONSTRAINT tbl_orders_account_id_fkey;
       public          postgres    false    224    4764    226            ;   �   x�M�]o�0F�_�K[�%ݤ"e8Q�	�X���ú���-zc��s�s�`v	`�ŨD�6����+��4�뉾�V[�t�O�XT��g2���VS�Q��ğ���5GF.�Y��5�<<-�O�W�L�I��}dy�.���:#�Nh�-���Q,ޕ[�����Y�zhe���l� ����e|� ^U�^��$,x![���kc�d��G�      2   �   x���1�0��9E.P'mB�C�L��ظ S;���&�RP���K����8F��Z8���MH�%�MoSw?��yq?Ƙ���s�kt�� �5�g�eh����H����i��O�L�HP�"��a��8�1L!��S�2�a*Yfɭ��u
�AY���e�8��:?f��{�Z-�R/��{�      9   b   x�3�t�/Q�+�MJ-R��K�L��KWHI%�9�i�FF��ƺF
V`�U�83=/1G�$?;5O��3Ə�D;�g��%��s��qqq g       ?      x������ � �      =      x������ � �      6      x������ � �      3   �   x��ͽ�@�z�] �{ �y*	���"��b�	?�o����Ƭ���|�ɏ��G��@ihL]��҄5G1C���c7{r���އ�i��.�[�)��.�k�����	���w��^�~SZ��h�7�H��<��BI��GR��#��R���B؆��A�     