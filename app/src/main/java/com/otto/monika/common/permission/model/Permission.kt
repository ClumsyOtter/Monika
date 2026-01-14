package com.otto.monika.common.permission.model


class Permission @JvmOverloads constructor(
    val name: String,
    val granted: Boolean,
    val shouldShowRequestPermissionRationale: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as Permission

        if (granted != that.granted) return false
        if (shouldShowRequestPermissionRationale != that.shouldShowRequestPermissionRationale) return false
        return name == that.name
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (if (granted) 1 else 0)
        result = 31 * result + (if (shouldShowRequestPermissionRationale) 1 else 0)
        return result
    }

    override fun toString(): String {
        return "Permission{" +
                "name='" + name + '\'' +
                ", granted=" + granted +
                ", shouldShowRequestPermissionRationale=" + shouldShowRequestPermissionRationale +
                '}'
    }
}
