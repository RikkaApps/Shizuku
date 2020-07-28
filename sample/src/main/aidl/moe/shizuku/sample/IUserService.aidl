package moe.shizuku.sample;

interface IUserService {

    void destroy() = 16777114; // Reserved destroy method

    void exit() = 1;

    int getPid() = 2;
}