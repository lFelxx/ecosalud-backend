package com.demo.ecosalud.enums;

/**
 * Roles que puede asumir un usuario. Varios roles por usuario se modelan con una colección de este enum
 * en la entidad, sin tablas de perfil por rol.
 */
public enum RolUser {

	USER,
	THERAPIST,
	ADMIN
}
