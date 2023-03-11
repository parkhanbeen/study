package com.helloboot.parkhanbeen.app;

public interface MemberRepository {
    Member findMember(String name);

    void increase(String name);

    default int countOf(String name) {
        final var member = findMember(name);
        return member == null ? 0 : member.getCount();
    }
}
